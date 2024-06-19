package com.ccwcar.printer.vo;

import lombok.Data;

import java.util.List;

@Data
public class PrinterReqVo {
    private String name;
    private List<String> resource;
}
