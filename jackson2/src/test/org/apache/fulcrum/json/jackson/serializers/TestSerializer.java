package org.apache.fulcrum.json.jackson.serializers;

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

import java.io.IOException;
import java.util.List;

import org.apache.fulcrum.json.jackson.example.Rectangle;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class TestSerializer extends StdSerializer<List<Rectangle>> {

    public TestSerializer() {
        super(List.class, false);
    }

    @Override
    public void serialize(List<Rectangle> data, JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
            JsonGenerationException {
        jgen.writeStartObject();
        for (int i = 0; i < data.size(); i++) {
            jgen.writeFieldName(data.get(i).getName());
            jgen.writeNumber(data.get(i).getSize());
        }
        jgen.writeEndObject();

    }
}