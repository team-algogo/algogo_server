package com.ssafy.algogo.program.group.config;

import com.ssafy.algogo.program.group.entity.GroupRole;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GroupAuthorize {

    GroupRole minRole() default GroupRole.USER;  // 기본값: USER
}