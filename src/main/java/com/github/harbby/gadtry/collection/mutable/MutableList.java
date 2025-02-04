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
package com.github.harbby.gadtry.collection.mutable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MutableList
{
    private MutableList() {}

    public static <T> List<T> asList(T first, T[] rest)
    {
        return MutableList.<T>builder().add(first).addAll(rest).build();
    }

    public static <T> List<T> copy(Iterable<? extends T> iterable)
    {
        if (iterable instanceof Collection) {
            return new ArrayList<>((Collection<? extends T>) iterable);
        }
        MutableList.Builder<T> builder = MutableList.builder();
        for (T it : iterable) {
            builder.add(it);
        }
        return builder.build();
    }

    @SafeVarargs
    public static <T> List<T> of(T... t)
    {
        return MutableList.<T>builder().addAll(t).build();
    }

    public static <T> Builder<T> builder()
    {
        return new Builder<>();
    }

    public static class Builder<T>
    {
        private Stream.Builder<T> builder = Stream.builder();

        public Builder<T> add(T t)
        {
            builder.add(t);
            return this;
        }

        @SafeVarargs
        public final Builder<T> addAll(T... ts)
        {
            for (T it : ts) {
                builder.add(it);
            }
            return this;
        }

        public Builder<T> addAll(Iterable<? extends T> iterable)
        {
            for (T it : iterable) {
                builder.add(it);
            }
            return this;
        }

        public Builder<T> addAll(Iterator<? extends T> iterator)
        {
            while (iterator.hasNext()) {
                builder.add(iterator.next());
            }
            return this;
        }

        public List<T> build()
        {
            return builder.build().collect(Collectors.toList());
        }
    }
}
