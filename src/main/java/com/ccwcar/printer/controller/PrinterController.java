package com.ccwcar.printer.controller;

import com.ccwcar.printer.utils.DownloadUtil;
import com.ccwcar.printer.utils.R;
import com.ccwcar.printer.vo.PrinterReqVo;
import org.springframework.web.bind.annotation.*;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.*;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CountDownLatch;

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
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        PrintService myPrinter = null;
        for (PrintService printService : printServices) {
            if (reqVo.getName().equals(printService.getName())) {
                myPrinter = printService;
                break;
            }
        }

        if (myPrinter == null) {
            return R.failed("找不到打印机");
        }

        DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
        PrintRequestAttributeSet attrSet = new HashPrintRequestAttributeSet();
        attrSet.add(MediaSizeName.ISO_A4); // 设置纸张大小为A4
//        attrSet.add(Chromaticity.COLOR); // 设置为彩色打印
//        attrSet.add(PrintQuality.HIGH); // 设置为高质量打印
        attrSet.add(OrientationRequested.PORTRAIT); // 设置为纵向打印
        attrSet.add(new Copies(1)); // 设置打印1份
        attrSet.add(Sides.ONE_SIDED); // 设置单面打印

        Set<String> failedResource = new HashSet<>();
        Set<String> successResource = new HashSet<>();
        Set<String> transferCompletedResource = new HashSet<>();

        for (String urlString : reqVo.getResource()) {
            System.out.println("print --->" + urlString);
            DocPrintJob printJob = myPrinter.createPrintJob();
            CountDownLatch latch = new CountDownLatch(1);
            printJob.addPrintJobListener(new PrintJobAdapter() {
                @Override
                public void printJobCompleted(PrintJobEvent pje) {
                    System.out.println("print success --->" + urlString);
                    successResource.add(urlString);
                    latch.countDown();
                }

                @Override
                public void printJobFailed(PrintJobEvent pje) {
                    System.out.println("print failed --->" + urlString);
                    failedResource.add(urlString);
                    latch.countDown();
                }

                @Override
                public void printDataTransferCompleted(PrintJobEvent pje) {
                    System.out.println("Data transfer completed for --->" + urlString);
                    transferCompletedResource.add(urlString);
                    latch.countDown();
                }

                @Override
                public void printJobNoMoreEvents(PrintJobEvent pje) {
                    System.out.println("No more events to be delivered for --->" + urlString);
                    // 这可能表示打印作业已经完成或失败，但没有触发相应事件
                    latch.countDown();
                }

            });

            try {
                InputStream is = DownloadUtil.download(urlString);
                Doc doc = new SimpleDoc(is, flavor, null);
                printJob.print(doc, attrSet);
                is.close();
                latch.await(); // 等待直到当前打印任务的监听器减少Latch计数
            } catch (Exception e) {
                e.printStackTrace();
                failedResource.add(urlString);
            }
        }

        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("failedResource", failedResource);
        resultMap.put("successResource", successResource);
        resultMap.put("transferCompletedResource", transferCompletedResource);
        return R.ok().setMsg("打印队列创建成功").setData(resultMap);
    }
}
