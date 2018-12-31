package com.rfacad;

@java.lang.annotation.Documented
@java.lang.annotation.Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
public abstract @interface Copyright
{
	public static final int FIRST_COPYRIGHT_YEAR=2018;
	public static final String defaultCopyright="Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0";
	public abstract String value() default "Copyright (c) 2018 Gerald Reno, Jr. All rights reserved. Licensed under Apache License 2.0";
}
