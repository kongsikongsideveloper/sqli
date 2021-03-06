/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.xream.sqli.builder;

import io.xream.sqli.core.Alias;
import io.xream.sqli.parser.Parsed;
import io.xream.sqli.parser.Parser;
import io.xream.sqli.util.BeanUtil;
import io.xream.sqli.util.ParserUtil;
import io.xream.sqli.util.SqliStringUtil;

/**
 * @Author Sim
 */
public interface KeyMapper {

    default void mapping(ScriptSplitable scriptSplitable, Alias alias, StringBuilder sb) {
        String[] keyArr = scriptSplitable.split(SqlScript.SPACE);
        int length = keyArr.length;
        for (int i = 0; i < length; i++) {
            String origin = keyArr[i].trim();

            String target = mapping(origin, alias);
            sb.append(target).append(SqlScript.SPACE);
        }
    }

    default String mapping(String key, Alias criteria) {

        if (SqliStringUtil.isNullOrEmpty(key))
            return key;
        if (key.contains(SqlScript.DOT)) {

            String[] arr = key.split("\\.");
            String alia = arr[0];
            String property = arr[1];

            String clzName = ParserUtil.getClzName(alia, criteria);

            Parsed parsed = Parser.get(clzName);
            if (parsed == null)
                return key;

            String p = parsed.getMapper(property);
            if (SqliStringUtil.isNullOrEmpty(p)) {
                return ((Criteria.ResultMapCriteria) criteria).getResultKeyAliaMap().get(key);
            }

            return parsed.getTableName(alia) + SqlScript.DOT + p;
        }

        if (criteria instanceof Criteria.ResultMapCriteria) {
            Parsed parsed = Parser.get(key);
            if (parsed != null) {
                return parsed.getTableName();
            }
        }

        if (criteria instanceof RefreshCondition){
            Parsed parsed = Parser.get(key);
            if (parsed != null) {
                return parsed.getTableName();
            }
        }

        Parsed parsed = ((CriteriaCondition)criteria).getParsed();
        if (parsed == null)
            return key;
        if (key.equals(BeanUtil.getByFirstLower(parsed.getClz().getSimpleName())))
            return parsed.getTableName();
        String value = parsed.getMapper(key);
        if (value == null)
            return key;
        return value;

    }
}
