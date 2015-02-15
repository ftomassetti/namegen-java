package com.github.ftomassetti;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.lang.model.element.Name;
import java.util.Random;


public class NameGeneratorTest 
    extends TestCase
{
  

    public void testApp()  {
        NameGenerator.Builder builder = new NameGenerator.Builder();
        builder.addSample("Lalala");
        builder.addSample("Lala");
        builder.addSample("Papa");
        NameGenerator nameGenerator = builder.build();
        Random r = new Random(100);
        System.out.println("NAME " + nameGenerator.name(r));
        System.out.println("NAME " + nameGenerator.name(r));
        System.out.println("NAME " + nameGenerator.name(r));
        System.out.println("NAME " + nameGenerator.name(r));
        System.out.println("NAME " + nameGenerator.name(r));
        System.out.println("NAME " + nameGenerator.name(r));
        System.out.println("NAME " + nameGenerator.name(r));
    }
}
