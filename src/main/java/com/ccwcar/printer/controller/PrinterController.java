package com.ccwcar.printer.controller;

import com.ccwcar.printer.utils.DownloadUtil;
import com.ccwcar.printer.utils.R;
import com.ccwcar.printer.vo.PrinterReqVo;
import org.springframework.web.bind.annotation.*;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("printer")
public class PrinterController {
    @GetMapping("scan")
    public R scan() {
        List<String> printerName = new ArrayList<>();
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printService : printServices) {
            String serviceName = printService.getName();
            printerName.add(serviceName);
        }
        return R.ok().setMsg("打印机扫描成功").setData(printerName);
    }

    @PostMapping("batch")
    public R print(@RequestBody PrinterReqVo reqVo) {
        List<String> printerName = new ArrayList<>();
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        PrintService myPrinter = null;
        for (PrintService printService : printServices) {
            String serviceName = printService.getName();
            printerName.add(serviceName);
            if (reqVo.getName().equals(serviceName)) {
                myPrinter = printService;
                break;
            }
        }

        if (myPrinter == null) {
            return R.failed("找不到打印机");
        }

        DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
        DocPrintJob printJob = myPrinter.createPrintJob();
        PrintRequestAttributeSet attrSet = new HashPrintRequestAttributeSet();
        attrSet.add(MediaSizeName.ISO_A4); // 设置纸张大小为A4
        attrSet.add(Chromaticity.COLOR); // 设置为彩色打印
//        attrSet.add(PrintQuality.HIGH); // 设置为高质量打印
        attrSet.add(OrientationRequested.PORTRAIT); // 设置为纵向打印
        attrSet.add(new Copies(1)); // 设置打印2份
        attrSet.add(Sides.TWO_SIDED_LONG_EDGE); // 设置双面打印（长边装订）
        List<String> failedResource = new ArrayList<>();
        List<String> successResource = new ArrayList<>();
        for (String urlString : reqVo.getResource()) {
            try {
                InputStream is = DownloadUtil.download(urlString);
                Doc doc = new SimpleDoc(is, flavor, null);
                printJob.print(doc, attrSet); // 打印文件
                is.close(); // 关闭文件输入流
                successResource.add(urlString);
            } catch (Exception e) {
                failedResource.add(urlString);
            }
        }
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("failedResource", failedResource);
        resultMap.put("successResource", successResource);
        return R.ok().setMsg("打印队列创建成功").setData(resultMap);
    }
}
