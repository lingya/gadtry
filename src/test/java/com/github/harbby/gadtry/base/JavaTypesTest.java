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
import org.junit.Assert;
import org.junit.Test;
import sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
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
}