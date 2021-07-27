package com.field.utils.fieldutils;

import com.field.utils.fieldutils.utils.FieldBaseUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;

@SpringBootTest
class FieldUtilsApplicationTests {

    @Test
    void contextLoads() {
        try {
            System.out.println(FieldBaseUtil.cronToString("0 0 6 * * ?"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
