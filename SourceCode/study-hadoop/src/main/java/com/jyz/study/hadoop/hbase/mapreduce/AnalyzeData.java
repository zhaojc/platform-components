package com.jyz.study.hadoop.hbase.mapreduce;

// cc AnalyzeData MapReduce job that reads the imported data and analyzes it.
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.io.compress.SnappyCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.jyz.study.hadoop.common.ConfigurationUtils;
import com.jyz.study.hadoop.common.Utils;

/**
 * 使用HBase作为数据源 TableMapReduceUtil.initTableMapperJob
 * -t testtable -c data:json -o output1
 * 分析habse数据
 * author1 : num1
 * author2 : num2
 * author3 : num3
 * 。。。
 * @author JoyoungZhang@gmail.com
 *
 */
public class AnalyzeData {

  private static final Log LOG = LogFactory.getLog(AnalyzeData.class);

  public static final String NAME = "AnalyzeData";
  public enum Counters { ROWS, COLS, ERROR, VALID }

  /**
   * Implements the <code>Mapper</code> that reads the data and extracts the
   * required information.
   */
  // vv AnalyzeData
  static class AnalyzeMapper extends TableMapper<Text, IntWritable> { // co AnalyzeData-1-Mapper Extend the supplied TableMapper class, setting your own output key and value types.

    private JSONParser parser = new JSONParser();
    private IntWritable ONE = new IntWritable(1);

    // ^^ AnalyzeData
    /**
     * Maps the input.
     *
     * @param row The row key.
     * @param columns The columns of the row.
     * @param context The task context.
     * @throws java.io.IOException When mapping the input fails.
     */
    // vv AnalyzeData
    @Override
    public void map(ImmutableBytesWritable row, Result columns, Context context)
    throws IOException {
      context.getCounter(Counters.ROWS).increment(1);
      String value = null;
      try {
        for (KeyValue kv : columns.list()) {
          context.getCounter(Counters.COLS).increment(1);
          value = Bytes.toStringBinary(kv.getValue());
          JSONObject json = (JSONObject) parser.parse(value);
          String author = (String) json.get("author"); // co AnalyzeData-2-Parse Parse the JSON data, extract the author and count the occurrence.
          // ^^ AnalyzeData
          if (context.getConfiguration().get("conf.debug") != null)
            System.out.println("Author: " + author);
          // vv AnalyzeData
          context.write(new Text(author), ONE);
          context.getCounter(Counters.VALID).increment(1);
        }
      } catch (Exception e) {
        e.printStackTrace();
        System.err.println("Row: " + Bytes.toStringBinary(row.get()) +
          ", JSON: " + value);
        context.getCounter(Counters.ERROR).increment(1);
      }
    }
    // ^^ AnalyzeData
    /*
       {
         "updated": "Mon, 14 Sep 2009 17:09:02 +0000",
         "links": [{
           "href": "http://www.webdesigndev.com/",
           "type": "text/html",
           "rel": "alternate"
         }],
         "title": "Web Design Tutorials | Creating a Website | Learn Adobe
             Flash, Photoshop and Dreamweaver",
         "author": "outernationalist",
         "comments": "http://delicious.com/url/e104984ea5f37cf8ae70451a619c9ac0",
         "guidislink": false,
         "title_detail": {
           "base": "http://feeds.delicious.com/v2/rss/recent?min=1&count=100",
           "type": "text/plain",
           "language": null,
           "value": "Web Design Tutorials | Creating a Website | Learn Adobe
               Flash, Photoshop and Dreamweaver"
         },
         "link": "http://www.webdesigndev.com/",
         "source": {},
         "wfw_commentrss": "http://feeds.delicious.com/v2/rss/url/
             e104984ea5f37cf8ae70451a619c9ac0",
         "id": "http://delicious.com/url/
             e104984ea5f37cf8ae70451a619c9ac0#outernationalist"
       }
    */
    // vv AnalyzeData
  }

  // ^^ AnalyzeData
  /**
   * Implements the <code>Reducer</code> part of the process.
   */
  // vv AnalyzeData
  static class AnalyzeReducer
  extends Reducer<Text, IntWritable, Text, IntWritable> { // co AnalyzeData-3-Reducer Extend a Hadoop Reducer class, assigning the proper types.

    // ^^ AnalyzeData
    /**
     * Aggregates the counts.
     *
     * @param key The author.
     * @param values The counts for the author.
     * @param context The current task context.
     * @throws IOException When reading or writing the data fails.
     * @throws InterruptedException When the task is aborted.
     */
    // vv AnalyzeData
    @Override
    protected void reduce(Text key, Iterable<IntWritable> values,
      Context context) throws IOException, InterruptedException {
      int count = 0;
      for (IntWritable one : values) count++; // co AnalyzeData-4-Count Count the occurrences and emit sum.
      // ^^ AnalyzeData
      if (context.getConfiguration().get("conf.debug") != null)
        System.out.println("Author: " + key.toString() + ", Count: " + count);
      // vv AnalyzeData
      context.write(key, new IntWritable(count));
    }
  }

  // ^^ AnalyzeData
  /**
   * Parse the command line parameters.
   *
   * @param args The parameters to parse.
   * @return The parsed command line.
   * @throws org.apache.commons.cli.ParseException When the parsing of the parameters fails.
   */
  private static CommandLine parseArgs(String[] args) throws ParseException {
    Options options = new Options();
    Option o = new Option("t", "table", true,
      "table to read from (must exist)");
    o.setArgName("table-name");
    o.setRequired(true);
    options.addOption(o);
    o = new Option("c", "column", true,
      "column to read data from (must exist)");
    o.setArgName("family:qualifier");
    options.addOption(o);
    o = new Option("o", "output", true,
      "the directory to write to");
    o.setArgName("path-in-HDFS");
    o.setRequired(true);
    options.addOption(o);
    options.addOption("d", "debug", false, "switch on DEBUG log level");
    CommandLineParser parser = new PosixParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch (Exception e) {
      System.err.println("ERROR: " + e.getMessage() + "\n");
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(NAME + " ", options, true);
      System.exit(-1);
    }
    if (cmd.hasOption("d")) {
      Logger log = Logger.getLogger("mapreduce");
      log.setLevel(Level.DEBUG);
      System.out.println("DEBUG ON");
    }
    return cmd;
  }

  /**
   * Main entry point.
   *
   * @param args  The command line parameters.
   * @throws Exception When running the job fails.
   */
  // vv AnalyzeData
  public static void main(String[] args) throws Exception {
    /*...*/
    // ^^ AnalyzeData
    Configuration conf = ConfigurationUtils.getHbaseConfiguration();
    
//    conf.setBoolean("mapred.compress.map.output", true);  
//    conf.setClass("mapred.map.output.compression.codec",GzipCodec.class, CompressionCodec.class);  

    
    String libjars = conf.get("tmpjars");
    String[] otherArgs =
      new GenericOptionsParser(conf, args).getRemainingArgs();
    CommandLine cmd = parseArgs(otherArgs);
    // check debug flag and other options
    if (cmd.hasOption("d")) conf.set("conf.debug", "true");
    // get details
    String table = cmd.getOptionValue("t");
    String column = cmd.getOptionValue("c");
    String output = cmd.getOptionValue("o");
    Utils.deleteIfExists(conf, output);

    // vv AnalyzeData
    Scan scan = new Scan(); // co AnalyzeData-5-Scan Create and configure a Scan instance.
    if (column != null) {
      byte[][] colkey = KeyValue.parseColumn(Bytes.toBytes(column));
      if (colkey.length > 1) {
        scan.addColumn(colkey[0], colkey[1]);
      } else {
        scan.addFamily(colkey[0]);
      }
    }
    Job job = new Job(conf, "Analyze data in " + table);
//    job.addFileToClassPath(new Path("file:/D:/Maven/repo/com/googlecode/json-simple/json-simple/1.1/json-simple-1.1.jar"));
//    DistributedCache.addFileToClassPath(new Path("/libs/hbase-0.92.1-cdh4.0.0-security.jar"), job.getConfiguration());
    job.setJarByClass(AnalyzeData.class);
    TableMapReduceUtil.initTableMapperJob(table, scan, AnalyzeMapper.class,
      Text.class, IntWritable.class, job, false); // co AnalyzeData-6-Util Set up the table mapper phase using the supplied utility.
    job.setReducerClass(AnalyzeReducer.class);
    job.setOutputKeyClass(Text.class); // co AnalyzeData-7-Output Configure the reduce phase using the normal Hadoop syntax.
    job.setOutputValueClass(IntWritable.class);
    job.setNumReduceTasks(1);
    FileOutputFormat.setOutputPath(job, new Path(output));
    FileOutputFormat.setCompressOutput(job, true);  
    FileOutputFormat.setOutputCompressorClass(job, SnappyCodec.class);  
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
  // ^^ AnalyzeData
}
