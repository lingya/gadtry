/*
 * Copyright (C) 2018 The GadTry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.harbby.gadtry.base;

import com.github.harbby.gadtry.aop.AopFactory;
import com.github.harbby.gadtry.function.Function1;
import org.junit.Assert;
import org.junit.Test;
import sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl;
import sun.reflect.generics.tree.TypeArgument;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static com.github.harbby.gadtry.base.Arrays.PRIMITIVE_TYPES;
import static com.github.harbby.gadtry.base.Throwables.noCatch;

public class JavaTypesTest
{
    @Test
    public void make()
            throws IOException
    {
        Type listType = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Type maType = JavaTypes.make(Map.class, new Type[] {String.class, listType}, null);

        Assert.assertTrue(Serializables.serialize((Serializable) maType).length > 0);
        Assert.assertTrue(maType.toString().length() > 0);
    }

    @Test
    public void makePrimitive()
    {
        try {
            JavaTypes.make(List.class, new Type[] {int.class}, null);
            Assert.fail();
        }
        catch (IllegalStateException e) {
            Assert.assertEquals(e.getMessage(), "Java Generic Type not support PrimitiveType");
        }

        try {
            JavaTypes.make(int.class, new Type[] {String.class}, null);
            Assert.fail();
        }
        catch (IllegalStateException e) {
            Assert.assertEquals(e.getMessage(), "rawType int must not PrimitiveType");
        }
    }

    @Test
    public void makeArrayType()
    {
        Type type = JavaTypes.make(List[].class, new Type[] {String.class}, null);
        Assert.assertEquals(type.getTypeName(), "java.util.List<java.lang.String>[]");
    }

    @Test
    public void isClassType()
    {
        Type type = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Assert.assertTrue(JavaTypes.isClassType(type));
        Assert.assertFalse(JavaTypes.isClassType(AopFactory.proxy(Type.class).byInstance(String.class).before(a -> {})));
        Assert.assertTrue(JavaTypes.isClassType(String.class));
    }

    @Test
    public void typeToClass()
    {
        Type type = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Assert.assertEquals(JavaTypes.typeToClass(type), List.class);
        Assert.assertEquals(JavaTypes.typeToClass(String.class), String.class);
    }

    @Test
    public void typeToClassGiveGenericArrayType()
    {
        Type type = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Type arrayType = GenericArrayTypeImpl.make(type);

        Assert.assertTrue(JavaTypes.isClassType(arrayType));
        Assert.assertEquals(JavaTypes.typeToClass(arrayType), List[].class);

        try {
            JavaTypes.typeToClass(AopFactory.proxy(Type.class).byInstance(String.class).before(a -> {}));
            Assert.fail();
        }
        catch (IllegalArgumentException e) {
            Assert.assertEquals("Cannot convert type to class", e.getMessage());
        }
    }

    @Test
    public void arrayTypeToClass()
    {
        Type type = JavaTypes.make(List[].class, new Type[] {String.class}, null);
        Assert.assertEquals(JavaTypes.typeToClass(type), List[].class);
    }

    @Test
    public void typeEqualsTest()
    {
        Type type = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Type type2 = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Assert.assertEquals(type, type2);
    }

    @Test
    public void hashCodeEqualsTest()
    {
        Type type = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Type type2 = JavaTypes.make(List.class, new Type[] {String.class}, null);
        Assert.assertEquals(type.hashCode(), type2.hashCode());
    }

    @Test
    public void getWrapperClass()
    {
        List<Class<?>> pack = PRIMITIVE_TYPES.stream()
                .map(JavaTypes::getWrapperClass)
                .collect(Collectors.toList());

        List<Class<?>> typeClassList = pack.stream().map(x -> noCatch(() -> {
            Field field = x.getField("TYPE");
            field.setAccessible(true);
            return (Class<?>) field.get(null);
        })).collect(Collectors.toList());

        Assert.assertEquals(PRIMITIVE_TYPES, typeClassList);

        try {
            JavaTypes.getWrapperClass(Object.class);
            Assert.fail();
        }
        catch (UnsupportedOperationException ignored) {
        }
    }

    @Test
    public void getClassGenericString()
    {
        Assert.assertEquals("Ljava/lang/Object;Lcom/github/harbby/gadtry/function/Function1<Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;Ljava/lang/String;>;Ljava/util/concurrent/Callable<Ljava/lang/Double;>;",
                JavaTypes.getClassGenericString(GenericTest.class));
    }

    @Test
    public void getClassGenericInfo()
    {
        Map<String, TypeArgument[]> types2 = JavaTypes.getClassGenericInfo(GenericTest.class);
        Assert.assertEquals(types2.size(), 3);
        TypeArgument[] a1 = types2.get(Function1.class.getName());
        Assert.assertEquals(a1.length, 2);
    }

    @Test
    public void getClassGenericInfoReturnEmpGiveJavaTypesTest()
    {
        Map<String, TypeArgument[]> types = JavaTypes.getClassGenericInfo(JavaTypesTest.class);
        Assert.assertTrue(types == Collections.EMPTY_MAP);
    }

    @Test
    public void getClassGenericTypes()
    {
        List<Type> types = JavaTypes.getClassGenericTypes(GenericTest.class);
        Assert.assertEquals(types.get(1), JavaTypes.make(Function1.class, new Type[] {JavaTypes.makeMapType(String.class, Integer.class), String.class}, null));
    }

    @Test
    public void getClassGenericTypesReturnEmpGiveJavaTypesTest()
    {
        List<Type> types = JavaTypes.getClassGenericTypes(JavaTypesTest.class);
        Assert.assertTrue(types == Collections.EMPTY_LIST);
    }

    public static class GenericTest
            implements Function1<Map<String, Integer>, String>, Callable<Double>
    {
        @Override
        public Double call()
                throws Exception
        {
            return null;
        }

        @Override
        public String apply(Map<String, Integer> o)
        {
            return null;
        }
    }
}
