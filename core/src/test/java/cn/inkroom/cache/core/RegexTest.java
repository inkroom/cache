package cn.inkroom.cache.core;

import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 墨盒
 * @date 2019/12/22
 */
class RegexTest {
    @Test
    void test() {
        Pattern pattern = Pattern.compile("^(.+)\\.(\\w+)\\((.*)\\)$");

        String id = "cn.inkroom.cache.spring.WaitProxyExampleBean.param(java.lang.String,int)";

        Matcher m = pattern.matcher(id);


        if (m.find()) {
            System.out.println("---");
            for (int i = 0; i <= m.groupCount(); i++) {
                System.out.println(m.group(i));

            }
            System.out.println("---");

        }


    }

}
