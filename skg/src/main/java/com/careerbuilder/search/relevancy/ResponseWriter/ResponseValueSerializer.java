/**Copyright 2015-2016 CareerBuilder, LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
**/

package com.careerbuilder.search.relevancy.responsewriter;

import com.careerbuilder.search.relevancy.model.ResponseValue;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class ResponseValueSerializer implements JsonSerializer<ResponseValue> {

    private final static String DEFAULT_VALUE_FIELD = "name";

    public JsonElement serialize(ResponseValue src, Type type, JsonSerializationContext context)
    {
        JsonObject resp = new JsonObject();
        if(src.normalizedValue == null) {
            resp.addProperty(DEFAULT_VALUE_FIELD, src.value);
        }
        else {
            src.normalizedValue.forEach((elem) -> resp.addProperty(elem.getKey(), elem.getValue()));
        }
        resp.addProperty("relatedness", src.relatedness);
        resp.addProperty("popularity", src.popularity);
        resp.addProperty("foreground_popularity", src.foreground_popularity);
        resp.addProperty("background_popularity", src.background_popularity);
        resp.add("compare", context.serialize(src.compare));
        return resp;
    }
}
