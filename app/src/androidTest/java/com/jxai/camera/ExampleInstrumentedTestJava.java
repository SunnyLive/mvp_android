package com.jxai.camera;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTestJava {

    @Test
    public void mapTest(){

        Map<String,String> maps = new HashMap<>();
        maps.put("123","231");
        Map.Entry<String, String> next = maps.entrySet().iterator().next();
        System.out.println("-------" + next.getValue());


        File file = new File("/aaa/bbb/test.txt");


        System.out.println("-------" + file.getName());

    }
}
