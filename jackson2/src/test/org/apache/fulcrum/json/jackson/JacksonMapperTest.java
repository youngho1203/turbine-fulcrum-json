package org.apache.fulcrum.json.jackson;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.fulcrum.json.JsonService;
import org.apache.fulcrum.json.Rectangle;
import org.apache.fulcrum.json.TestClass;
import org.apache.fulcrum.testcontainer.BaseUnit4Test;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;



/**
 * Jackson2 JSON Test
 * 
 * Test without type setting 
 * 
 * @author gk
 * @version $Id$
 */
public class JacksonMapperTest extends BaseUnit4Test {
    private final String preDefinedOutput = "{\"container\":{\"cf\":\"Config.xml\"},\"configurationName\":\"Config.xml\",\"name\":\"mytest\"}";
    private JsonService sc = null;
    Logger logger;

    @Before
    public void setUp() throws Exception {
        sc = (JsonService) this.lookup(JsonService.ROLE);
        logger = new ConsoleLogger(ConsoleLogger.LEVEL_DEBUG);
    }

    @Test
    public void testSerialize() throws Exception {
        String serJson = sc.ser(new TestClass("mytest"));
        assertEquals("Serialization failed ", preDefinedOutput, serJson);
    }

    @Ignore
    public void testDeSerialize() throws Exception {
        String serJson = sc.ser(new TestClass("mytest"));
        Object deson = sc.deSer(serJson, TestClass.class);
        assertEquals("DeSer failed ", TestClass.class, deson.getClass());
    }
    @Test
    public void testSerializeDateWithDefaultDateFormat() throws Exception {
        Map<String, Date> map = new HashMap<String, Date>();
        map.put("date", Calendar.getInstance().getTime());
        String serJson = sc.ser(map);
        assertTrue("Serialize with Adapater failed ",
                serJson.matches("\\{\"date\":\"\\d\\d/\\d\\d/\\d{4}\"\\}"));
    }
    @Test
    public void testDeSerializeDate() throws Exception {
        Map<String, Date> map = new HashMap<String, Date>();
        map.put("date", Calendar.getInstance().getTime());
        String serJson = ((Jackson2MapperService) sc).ser(map, Map.class);
        Map serDate = sc.deSer(serJson, Map.class);
        assertEquals("Date DeSer failed ", String.class, serDate.get("date")
                .getClass());
    }
    @Test
    public void testSerializeWithCustomFilter() throws Exception {
        Bean bean = new Bean();
        bean.setName("joe");
        bean.setAge(12);
        String filteredBean  = sc.serializeOnlyFilter(bean, Bean.class, "name");
        assertEquals("Ser filtered Bean failed ", "{\"name\":\"joe\"}", filteredBean);

        Rectangle rectangle = new Rectangle(5, 10);
        rectangle.setName("jim");
        String filteredRectangle  = sc.serializeOnlyFilter(rectangle,
                Rectangle.class, "w", "name");
        assertEquals("Ser filtered Rectangle failed ",
                "{\"w\":5,\"name\":\"jim\"}", filteredRectangle);
    }
    
    @Test
    public void testSerializationCollectionWithFilter() throws Exception {

        List<Bean> beanList = new ArrayList<Bean>();
        for (int i = 0; i < 10; i++) {
            Bean bean = new Bean();
            bean.setName("joe" + i);
            bean.setAge(i);
            beanList.add(bean);
        }
        String filteredResult = sc.serializeOnlyFilter(beanList, Bean.class, "name",
                "age");
        assertEquals(
                "Serialization of beans failed ",
                "[{'name':'joe0','age':0},{'name':'joe1','age':1},{'name':'joe2','age':2},{'name':'joe3','age':3},{'name':'joe4','age':4},{'name':'joe5','age':5},{'name':'joe6','age':6},{'name':'joe7','age':7},{'name':'joe8','age':8},{'name':'joe9','age':9}]",
                filteredResult.replace('"', '\''));
    }
    
    @Test
    public void testTwoSerializationCollectionWithTwoDifferentFilter() throws Exception {

        List<Bean> beanList = new ArrayList<Bean>();
        for (int i = 0; i < 10; i++) {
            Bean bean = new Bean();
            bean.setName("joe" + i);
            bean.setAge(i);
            beanList.add(bean);
        }
        String filteredResult = sc.serializeOnlyFilter(beanList, Bean.class, "name",
                "age");
        System.out.println( filteredResult );
        assertEquals("Serialization of beans failed ",
                "[{'name':'joe0','age':0},{'name':'joe1','age':1},{'name':'joe2','age':2},{'name':'joe3','age':3},{'name':'joe4','age':4},{'name':'joe5','age':5},{'name':'joe6','age':6},{'name':'joe7','age':7},{'name':'joe8','age':8},{'name':'joe9','age':9}]",
        filteredResult.replace('"', '\''));
        filteredResult = sc.serializeOnlyFilter(beanList, Bean.class, "name");
        System.out.println( filteredResult );
        assertEquals("Serialization of beans failed ",
                     "[{'name':'joe0'},{'name':'joe1'},{'name':'joe2'},{'name':'joe3'},{'name':'joe4'},{'name':'joe5'},{'name':'joe6'},{'name':'joe7'},{'name':'joe8'},{'name':'joe9'}]",
             filteredResult.replace('"', '\''));
    }
    
    /** This may be a bug in jackson, the filter is not exchanged if the same class is matched again
    * 
    * first it may be com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap.Empty.serializerFor(Class<?>)
    * and then 
    * com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap.Single.serializerFor(Class<?>)
    * which returns a serializer
    * **/
    @Test
    public void testTwoSerializationCollectionNoAndWithFilter() throws Exception {
        List<Bean> beanList = new ArrayList<Bean>();
        for (int i = 0; i < 4; i++) {
            Bean bean = new Bean();
            bean.setName("joe" + i);
            bean.setAge(i);
            beanList.add(bean);
        }
        String filteredResult = sc.ser(beanList, Bean.class);//unfiltered
        System.out.println( filteredResult );
        assertEquals("First unfiltered serialization of beans failed ",
                "[{'name':'joe0','age':0,'profession':''},{'name':'joe1','age':1,'profession':''},{'name':'joe2','age':2,'profession':''},{'name':'joe3','age':3,'profession':''}]",
        filteredResult.replace('"', '\''));
        
        filteredResult = sc.serializeOnlyFilter(beanList, Bean.class, "name");
        System.out.println( filteredResult );
        // this may be a bug in jackson, serializer is reused, if not cleaned up
        assertNotEquals("[{'name':'joe0'},{'name':'joe1'},{'name':'joe2'},{'name':'joe3'}]",
        filteredResult.replace('"', '\''));
        
        // cleaning requires, that you have to provide some other type, which is different from the (typed) source object, 
        // providing just new ArrayList<Bean>() only will not help, but an anonymous class may be sufficient.
        // A simple object will do it, this resets to an unknown serializer, which eventaully does clean up  the serializer cache.
        sc.serializeOnlyFilter(new Object(), new String[]{});
        filteredResult = sc.serializeOnlyFilter(beanList, Bean.class, "name");
        System.out.println( filteredResult );
        assertEquals("Second filtered serialization of beans failed ", "[{'name':'joe0'},{'name':'joe1'},{'name':'joe2'},{'name':'joe3'}]",
        filteredResult.replace('"', '\''));
    }
    
    @Test
    public void testSetMixin() {
        Bean src = new Bean();
        src.setName("joe");
        src.setAge( 99 );
        src.setProfession("runner");
        //
        // profession was already set to ignore, does not change
        String result = null;
        try
        {
            result = ((Jackson2MapperService)sc).withMixinModule(src, "mixinbean", Bean.class, BeanMixin.class );
            assertEquals(
                         "Ser filtered Bean failed ",
                         "{\"name\":\"joe\"}",
                         result);
            // clean up buffer is not sufficient..
            sc.serializeOnlyFilter(new Object(), new String[]{});
            
            // .. this assert result is not to be expected!!!
            result = ((Jackson2MapperService)sc).withMixinModule(src, "mixin2bean", Bean.class, BeanMixin2.class );
            assertEquals(
                         "Ser filtered Bean failed ",
                         "{\"name\":\"joe\"}",
                         result);
            // clean up of mixin and buffer required
            
            // clean up mixins 
             ((Jackson2MapperService)sc).setMixins(Bean.class, BeanMixin2.class );
             
             // clean up buffer 
             sc.serializeOnlyFilter(new Object(), new String[]{});
             
//           Map<Class<?>, Class<?>> sourceMixins = new HashMap<Class<?>, Class<?>>(1);
//           sourceMixins.put( Bean.class,BeanMixin2.class );
//           ((Jackson2MapperService)sc).getMapper().setMixIns( sourceMixins  );
             result =sc.ser( src, Bean.class );
             assertEquals(
                     "Ser filtered Bean failed ",
                     "{\"age\":99,\"profession\":\"runner\"}",
                     result);
        }
        catch ( JsonProcessingException e )
        {
            logger.error( "err",e );
           fail();
        }
        catch ( Throwable e )
        {
            logger.error( "err",e );
            fail();
        }

    }

    @Test
    public void testDeserializationCollectionWithFilter() throws Exception {

        List<Bean> beanList = new ArrayList<Bean>();
        for (int i = 0; i < 10; i++) {
            Bean bean = new Bean();
            bean.setName("joe" + i);
            bean.setAge(i);
            beanList.add(bean);
        }
        String filteredResult = sc.serializeOnlyFilter(beanList, Bean.class, "name",
                "age");
        List<Bean> beanList2 = (List<Bean>) ((Jackson2MapperService) sc)
                .deSerCollectionWithType(filteredResult, List.class, Bean.class);
        assertTrue("DeSer failed ", beanList2.size() == 10);
        for (Bean bean : beanList2) {
            assertEquals("DeSer failed ", Bean.class, bean.getClass());
        }
    }
    @Test
    public void testDeserializationUnTypedCollectionWithFilter()
            throws Exception {

        List<Bean> beanList = new ArrayList<Bean>();
        for (int i = 0; i < 10; i++) {
            Bean bean = new Bean();
            bean.setName("joe" + i);
            bean.setAge(i);
            beanList.add(bean);
        }
        String filteredResult = sc.serializeOnlyFilter(beanList, Bean.class, "name",
                "age");
        Object beanList2 = sc.deSer(filteredResult, List.class);
        assertTrue("DeSer failed ", beanList2 instanceof List);
        assertTrue("DeSer failed ", ((List) beanList2).size() == 10);
        for (int i = 0; i < ((List) beanList2).size(); i++) {
            assertTrue("DeSer failed ",
                    ((List) beanList2).get(i) instanceof Map);
            assertTrue(
                    "DeSer failed ",
                    ((Map) ((List) beanList2).get(i)).get("name").equals(
                            "joe" + i));
        }
    }
    
    @Test
    public void testSerializeWithMixin() throws Exception {
        Rectangle rectangle = new Rectangle(5, 10);
        rectangle.setName("jim");
        String filteredRectangle = sc
                .addAdapter("M4RMixin", Rectangle.class, Mixin.class).ser(rectangle);
        assertEquals("Ser failed ", "{\"width\":5}", filteredRectangle);
    }
    @Test
    public void testSerializeWith2Mixins() throws Exception {
        Bean bean = new Bean();
        bean.setName("joe");
        Rectangle filteredRectangle = new Rectangle(5, 10);
        filteredRectangle.setName("jim");

        String serRect = sc.addAdapter("M4RMixin2", Rectangle.class,
                Mixin2.class).ser(filteredRectangle);
        assertEquals("Ser failed ", "{\"name\":\"jim\",\"width\":5}", serRect);

        String filteredBean = sc.serializeOnlyFilter(bean, Bean.class, "name");
        assertEquals("Ser filtered Bean failed ", "{\"name\":\"joe\"}", filteredBean);
    }
    @Test
    public void testSerializationCollectionWithMixin() throws Exception {

        List<Bean> beanList = new ArrayList<Bean>();
        for (int i = 0; i < 10; i++) {
            Bean bean = new Bean();
            bean.setName("joe" + i);
            bean.setAge(i);
            beanList.add(bean);
        }
        String filterResult = sc.addAdapter("M4RMixin", Bean.class, BeanMixin.class)
                .ser(beanList);
        assertEquals(
                "Serialization of beans failed ",
                "[{'name':'joe0'},{'name':'joe1'},{'name':'joe2'},{'name':'joe3'},{'name':'joe4'},{'name':'joe5'},{'name':'joe6'},{'name':'joe7'},{'name':'joe8'},{'name':'joe9'}]",
                filterResult.replace('"', '\''));
    }
    
    @Test
    public void testSerializationBeanWithMixin() throws Exception {
        Bean bean = new Bean();
        bean.setName("joe1");
        bean.setAge(1);
        String filterResult = sc.addAdapter("M4RMixin", Bean.class, BeanMixin.class)
                .ser(bean);
        logger.debug("filterResult: "+ filterResult.toString());
    }
    
    @Test
    public void testDeSerUnQuotedObject() throws Exception {
        String jsonString = "{name:\"joe\"}";
        Bean result = sc.deSer(jsonString, Bean.class);
        assertTrue("expected bean object!", result instanceof Bean);
    }
    
    public void testDeserializationCollection2() throws Exception {
        List<Rectangle> rectList = new ArrayList<Rectangle>(); 
        for (int i = 0; i < 10; i++) {
            Rectangle filteredRect = new Rectangle(i, i, "rect" + i);
            rectList.add(filteredRect);
        }
        String serColl = sc.ser(rectList);
        Collection<Rectangle> resultList0 =  ((Jackson2MapperService) sc) .deSerCollectionWithType(serColl, ArrayList.class, Rectangle.class);
        
        for (int i = 0; i < 10; i++) {
            assertEquals("deser reread size failed", (i * i), ((List<Rectangle>)resultList0)
                    .get(i).getSize());
        }
    }
    @Test
    public void testDeSerializationCollectionWithMixin() throws Exception {

        List<Bean> beanList = new ArrayList<Bean>();
        for (int i = 0; i < 10; i++) {
            Bean bean = new Bean();
            bean.setName("joe" + i);
            bean.setAge(i);
            beanList.add(bean);
        }
        String filterResult = sc.addAdapter("M4RMixin", Bean.class, BeanMixin.class)
                .ser(beanList);
        Object beanList2 = sc.deSer(filterResult,
                List.class);
        assertTrue("DeSer failed ", beanList2 instanceof List);
        assertTrue("DeSer failed ", ((List) beanList2).size() == 10);
        for (int i = 0; i < ((List) beanList2).size(); i++) {
            assertTrue("DeSer failed ",
                    ((List) beanList2).get(i) instanceof Map);
            assertTrue(
                    "DeSer failed ",
                    ((Map) ((List) beanList2).get(i)).get("name").equals(
                            "joe" + i));
        }
    }
    @Test
    public void testCollectionWithMixins() throws Exception {
        List<Object> components = new ArrayList<Object>();
        components.add(new Rectangle(25, 3));
        components.add(new Rectangle(250, 30));
        for (int i = 0; i < 3; i++) {
            Bean filteredBean = new Bean();
            filteredBean.setName("joe" + i);
            filteredBean.setAge(i);
            components.add(filteredBean);
        }

        sc.addAdapter("M4RMixin", Rectangle.class, Mixin.class).addAdapter(
                "M4BeanRMixin", Bean.class, BeanMixin.class);
        String serRect = sc.ser(components);
        assertEquals(
                "DeSer failed ",
                "[{'width':25},{'width':250},{'name':'joe0'},{'name':'joe1'},{'name':'joe2'}]",
                serRect.replace('"', '\''));
        
        // adding h and name for first two items, adding width for beans
        String deSerTest = "[{\"width\":25,\"age\":99, \"h\":50,\"name\":\"rect1\"},{\"width\":250,\"name\":\"rect2\"},{\"name\":\"joe0\"},{\"name\":\"joe1\"},{\"name\":\"joe2\"}]";
        
        List typeRectList = new ArrayList(); //empty
        // could not use Mixins here, but Adapters are still set
        Collection<Rectangle> resultList0 =  sc.deSerCollection(deSerTest, typeRectList, Rectangle.class);
        logger.debug("resultList0 class:" +resultList0.getClass());
        for (int i = 0; i < 5; i++) {
            // name and h should be null as it is ignored,  cft. Mixin
            assertTrue(((List<Rectangle>)resultList0).get(i).getName()==null);
            assertTrue(((List<Rectangle>)resultList0).get(i).getH()==0);
        }
        // could not use Mixins here, but Adapters are still set
        Collection<Bean> resultList1 =  sc.deSerCollection(deSerTest, typeRectList, Bean.class);
        logger.debug("resultList1 class:" +resultList1.getClass());
        for (int i = 0; i < 5; i++) {
            logger.debug("resultList1 "+i+ " name:"+((List<Bean>)resultList1).get(i).getName());
            // name should NOT be null, age should be ignored, cft. BeanMixin
            assertTrue(((List<Bean>)resultList1).get(i).getName()!=null);
            assertTrue(((List<Bean>)resultList1).get(i).getAge()==0);
        }
        ((Initializable)sc).initialize();// reinit to default settings
        Collection<Rectangle> resultList3 =  sc.deSerCollection(deSerTest, typeRectList, Rectangle.class);
        // h should be set again without Mixin
        assertTrue(((List<Rectangle>)resultList3).get(0).getH()!=0);
        for (int i = 0; i < 5; i++) {
            // name should be set without Mixin
            assertTrue(((List<Rectangle>)resultList3).get(i).getName()!=null);
        }
    }
    
    @Test
    public void testSerializeListWithWrapper()  {
        try
        {
            Bean bean = new Bean();
            bean.setName("joe");
            bean.setAge(12);
            String filteredBean  = sc.serializeOnlyFilter(bean, Bean.class, "name");
            assertEquals("Ser filtered Bean failed ", "{\"name\":\"joe\"}", filteredBean);

            Rectangle rectangle = new Rectangle(5, 10);
            rectangle.setName("quadro");
            String filteredRectangle  = sc.serializeOnlyFilter(rectangle,
                    Rectangle.class, "w", "name");
            assertEquals("Ser filtered Rectangle failed ",
                    "{\"w\":5,\"name\":\"quadro\"}", filteredRectangle);
            
            Bean bean2 = new Bean();
            bean2.setName("jim");
            bean2.setAge(92);
            List<Bean> beans = Arrays.asList( bean, bean2 );
            List<Rectangle> rectangles = Arrays.asList( rectangle );
            List wrapper = new ArrayList();
            wrapper.addAll( beans ); wrapper.addAll( rectangles );
            
            //String wrappedLists =  sc.serializeOnlyFilter( wrapper, "name" );
            String jsonResult =  sc.ser( wrapper );
            // res:wrappedLists:[{"name":"joe","age":12,"profession":""},{"w":5,"h":10,"name":"jim","size":50}]
            logger.debug( "jsonResult provided wrapper:" +jsonResult );
            List listResult = (List) ((Jackson2MapperService)sc).deSerCollectionWithType( jsonResult, ArrayList.class,Object.class );
            logger.debug( " provided wrapper lists:" +listResult );
            
            String jsonResult2 =  ((Jackson2MapperService)sc).ser( false, bean, bean2, rectangle );
            logger.debug( "jsonResult2 bean, rectangle / no collection:" +jsonResult2 );
            List listResult2 = (List) ((Jackson2MapperService)sc).deSerCollectionWithType( jsonResult2, ArrayList.class,Object.class );
            logger.debug( "bean, rectangle / no collection lists:" +listResult2 );
            assertTrue( jsonResult.equals( jsonResult2 ) );
            listResult2.removeAll( listResult );
            assertTrue( listResult2.isEmpty() );
            
            String jsonResult3 =  ((Jackson2MapperService)sc).ser( false, (Collection)beans, (Collection)rectangles );
            // this wrape anything
            logger.debug( "jsonResult3 raw lists:" +jsonResult3 ); 
            List<List> listResult3 = (List) ((Jackson2MapperService)sc).deSerCollectionWithType( jsonResult3, ArrayList.class,List.class );
            logger.debug( "raw lists:" +listResult3 );
            listResult3.get( 0 ).removeAll( listResult );
            listResult3.get( 1 ).removeAll( listResult );
            assertTrue( listResult3.get( 0 ).isEmpty() );
            assertTrue( listResult3.get( 1 ).isEmpty() );
            
            // this does not get any information, just to demonstrate
            TypeReference<List<?>> typeRef = new TypeReference<List<?>>(){};
            String jsonResult4 = ((Jackson2MapperService)sc).serCollectionWithTypeReference(wrapper,typeRef, false);
            logger.debug( "jsonResult4 typereference:" +jsonResult4 );
            List<Object> listResult4 = (List) ((Jackson2MapperService)sc).deSerCollectionWithType( jsonResult4, ArrayList.class,Object.class );
            logger.debug( "typereference lists:" +listResult4 );
            listResult4.removeAll( listResult );
            assertTrue( listResult4.isEmpty() );

            ((Jackson2MapperService)sc).getMapper().enable(SerializationFeature.WRAP_ROOT_VALUE);
            String jsonResult5 =  sc.ser( wrapper );
            // res:wrappedLists:[{"name":"joe","age":12,"profession":""},{"w":5,"h":10,"name":"jim","size":50}]
            logger.debug( "jsonResult5 wrap root:" +jsonResult5 );
            
            ((Jackson2MapperService)sc).getMapper().configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            List<Object> listResult5 = (List) ((Jackson2MapperService)sc)
                            .deSerCollectionWithType( jsonResult4, ArrayList.class,Object.class );
            logger.debug( "wrap root lists:" +listResult5 );
            listResult5.removeAll( listResult );
            assertTrue( listResult5.isEmpty() );
            List<Object> listResult51 = (List) ((Jackson2MapperService)sc)
                            .deSerCollectionWithTypeReference( jsonResult5, new TypeReference<List<?>>() {} );
            logger.debug( "wrap root lists typereferenced:" +listResult51 );
            ((Map<String, List>)listResult51.get( 0 )).values().iterator().next().removeAll( listResult );
            assertTrue( ((Map<String, List>)listResult51.get( 0 )).values().iterator().next().isEmpty() );
            
            
        }
        catch ( Exception e )
        {
            e.printStackTrace();
           fail();
        } 
    }


    public static abstract class Mixin2 {
        void MixIn2(int w, int h) {
        }

        @JsonProperty("width")
        abstract int getW(); // rename property

        @JsonIgnore
        abstract int getH();

        @JsonIgnore
        abstract int getSize(); // exclude

        abstract String getName();
    }

    public static abstract class BeanMixin {
        BeanMixin() {
        }

        @JsonIgnore
        abstract int getAge();

        @JsonIgnore
        String profession; // exclude

        @JsonProperty
        abstract String getName();//
    }
    public static abstract class BeanMixin2 extends Bean {
        BeanMixin2() {
        }
        @JsonIgnore
        public abstract String getName();//
    }

}
