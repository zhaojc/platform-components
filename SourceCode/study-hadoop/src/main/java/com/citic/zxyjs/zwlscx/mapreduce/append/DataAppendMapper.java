package com.citic.zxyjs.zwlscx.mapreduce.append;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.DefaultStringifier;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.citic.zxyjs.zwlscx.bean.Field;
import com.citic.zxyjs.zwlscx.bean.Task;
import com.citic.zxyjs.zwlscx.mapreduce.JobGenerator;
import com.citic.zxyjs.zwlscx.xml.Separator;

/**
 * Append操作的mapper
 * @author JoyoungZhang@gmail.com
 *
 */
public class DataAppendMapper extends Mapper<LongWritable, Text, ImmutableBytesWritable, Put> { // co

    private Task task;
    private boolean init;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
	super.setup(context);
	this.task = DefaultStringifier.load(context.getConfiguration(), JobGenerator.APPEND_JOB_TASK, Task.class);
	this.init = context.getConfiguration().getBoolean("init", false);
    }

    @Override
    public void map(LongWritable offset, Text line, Context context) throws IOException {
	String lineString = line.toString();
	byte[] rowkey = Bytes.toBytes(lineString.hashCode()); // co
	Put put = new Put(rowkey);
	String[] tokens = lineString.split(Separator.SEP_COMMA);
	List<Field> fields = task.getRightSource().getFields();
	for(int i=0;i<tokens.length;i++){
	    put.add(Bytes.toBytes("cf1"), Bytes.toBytes(fields.get(i).getName()), Bytes.toBytes(tokens[i])); 
	}
	try {
	    context.write(new ImmutableBytesWritable(rowkey), put);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }
}