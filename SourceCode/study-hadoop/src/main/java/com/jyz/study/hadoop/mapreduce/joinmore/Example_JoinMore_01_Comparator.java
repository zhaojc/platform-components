package com.jyz.study.hadoop.mapreduce.joinmore;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

import com.jyz.study.hadoop.common.TextPair;

/**
 * 分组比较器
 * @author JoyoungZhang@gmail.com
 *
 */
public class Example_JoinMore_01_Comparator extends WritableComparator {
    public Example_JoinMore_01_Comparator() {
	super(TextPair.class, true);
    }

    @SuppressWarnings("unchecked")
    public int compare(WritableComparable a, WritableComparable b) {
	TextPair t1 = (TextPair) a;
	TextPair t2 = (TextPair) b;
	return t1.getFirst().compareTo(t2.getFirst());
    }
}