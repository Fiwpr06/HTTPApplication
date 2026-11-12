package com.fiwpr06.httpapplication.server.service;

import com.fiwpr06.httpapplication.common.constant.HttpStatus;
import com.fiwpr06.httpapplication.common.model.HttpResponse;
import com.fiwpr06.httpapplication.common.util.DateTimeUtil;
import com.fiwpr06.httpapplication.common.util.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Service chuyen trach tu dong sinh trang HTML danh muc tep tin (Auto Directory Indexing)
 * khi nguoi dung truy cap vao thu muc khong co index.html (chuan Apache / Nginx).
 */
public class DirectoryIndexService {

    public HttpResponse render(File dir, String requestPath) {
        String cleanPath = requestPath.endsWith("/") ? requestPath : requestPath + "/";
        File[] files = dir.listFiles();
        if (files == null) {
            files = new File[0];
        }

        // Sap xep: Thu muc len dau, sau do theo Alphabet
        Arrays.sort(files, (a, b) -> {
            if (a.isDirectory() != b.isDirectory()) {
                return a.isDirectory() ? -1 : 1;
            }
            return a.getName().compareToIgnoreCase(b.getName());
        });

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='vi'><head><meta charset='utf-8'>");
        sb.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        sb.append("<title>Index of ").append(cleanPath).append(" - MediaHub</title>");
        sb.append("<style>");
        sb.append("body{background:#F8FAFC;color:#0F172A;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;margin:0;padding:24px 20px;}");
        sb.append(".container{max-width:960px;margin:0 auto;background:#FFFFFF;border:1px solid #CBD5E1;border-radius:12px;box-shadow:0 4px 6px -1px rgba(0,0,0,0.05);overflow:hidden;}");
        sb.append(".header{background:#F1F5F9;border-bottom:1px solid #CBD5E1;padding:16px 24px;display:flex;align-items:center;justify-content:space-between;}");
        sb.append(".header h1{margin:0;font-size:18px;color:#1E3A8A;display:flex;align-items:center;gap:8px;}");
        sb.append(".header .count{font-size:13px;color:#64748B;font-weight:600;}");
        sb.append(".btn-home{background:#1E3A8A;color:#FFFFFF;text-decoration:none;padding:6px 14px;border-radius:6px;font-size:12.5px;font-weight:600;}");
        sb.append("table{width:100%;border-collapse:collapse;font-size:13.5px;}");
        sb.append("th{background:#F8FAFC;color:#475569;font-weight:700;text-align:left;padding:12px 18px;border-bottom:1px solid #E2E8F0;font-size:12px;text-transform:uppercase;letter-spacing:0.5px;}");
        sb.append("td{padding:12px 18px;border-bottom:1px solid #F1F5F9;vertical-align:middle;}");
        sb.append("tr:hover td{background:#F8FAFC;}");
        sb.append("a.name{color:#0284C7;text-decoration:none;font-weight:600;}");
        sb.append("a.name:hover{text-decoration:underline;color:#0369A1;}");
        sb.append(".badge-tag{display:inline-block;padding:2px 8px;border-radius:4px;font-size:11px;font-weight:700;font-family:'Consolas',monospace;}");
        sb.append(".badge-dir{background:#EFF6FF;color:#1E3A8A;}");
        sb.append(".badge-audio{background:#ECFDF5;color:#059669;}");
        sb.append(".badge-video{background:#FEF3C7;color:#D97706;}");
        sb.append(".badge-pdf{background:#FEE2E2;color:#DC2626;}");
        sb.append(".badge-docx{background:#DBEAFE;color:#1D4ED8;}");
        sb.append(".badge-file{background:#F1F5F9;color:#475569;}");
        sb.append(".meta{color:#64748B;font-family:'Consolas',monospace;font-size:12px;}");
        sb.append(".footer{padding:14px 24px;background:#F8FAFC;border-top:1px solid #E2E8F0;font-size:12px;color:#94A3B8;display:flex;justify-content:space-between;}");
        sb.append("</style></head><body>");

        sb.append("<div class='container'>");
        sb.append("<div class='header'>");
        sb.append("<div>");
        sb.append("<h1>Index of ").append(cleanPath).append("</h1>");
        sb.append("<div class='count'>").append(files.length).append(" mục trong thư mục này</div>");
        sb.append("</div>");
        sb.append("<a href='/' class='btn-home'>Về Trang Chủ</a>");
        sb.append("</div>");

        sb.append("<table>");
        sb.append("<thead><tr><th>Loại</th><th>Tên Tệp / Thư Mục</th><th style='text-align:right;'>Kích Thước</th><th>Ngày Sửa Đổi</th></tr></thead>");
        sb.append("<tbody>");

        // Link thư mục cha nếu không phải ở thư mục gốc
        if (!"/".equals(cleanPath)) {
            String parentPath = cleanPath.substring(0, cleanPath.length() - 1);
            int lastSlash = parentPath.lastIndexOf('/');
            String parent = lastSlash >= 0 ? parentPath.substring(0, lastSlash + 1) : "/";
            if (parent.isEmpty()) parent = "/";
            sb.append("<tr>");
            sb.append("<td><span class='badge-tag badge-dir'>FOLDER</span></td>");
            sb.append("<td colspan='3'><a class='name' href='").append(parent).append("'>.. (Thư mục cha)</a></td>");
            sb.append("</tr>");
        }

        for (File f : files) {
            String name = f.getName();
            String link = cleanPath + name;
            boolean isDir = f.isDirectory();
            String tagClass = "badge-file";
            String tagText = "FILE";

            if (isDir) {
                tagClass = "badge-dir";
                tagText = "FOLDER";
                link += "/";
            } else {
                String lower = name.toLowerCase();
                if (lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".ogg")) {
                    tagClass = "badge-audio";
                    tagText = "AUDIO";
                } else if (lower.endsWith(".mp4") || lower.endsWith(".webm") || lower.endsWith(".mkv")) {
                    tagClass = "badge-video";
                    tagText = "VIDEO";
                } else if (lower.endsWith(".pdf")) {
                    tagClass = "badge-pdf";
                    tagText = "PDF";
                } else if (lower.endsWith(".docx") || lower.endsWith(".doc")) {
                    tagClass = "badge-docx";
                    tagText = "DOCX";
                } else if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".webp") || lower.endsWith(".gif")) {
                    tagClass = "badge-audio";
                    tagText = "IMAGE";
                }
            }

            sb.append("<tr>");
            sb.append("<td><span class='badge-tag ").append(tagClass).append("'>").append(tagText).append("</span></td>");
            sb.append("<td><a class='name' href='").append(link).append("'>").append(name).append(isDir ? "/" : "").append("</a></td>");
            sb.append("<td style='text-align:right;' class='meta'>").append(isDir ? "-" : FileUtils.formatFileSize(f.length())).append("</td>");
            sb.append("<td class='meta'>").append(DateTimeUtil.formatHttpDate(f.lastModified())).append("</td>");
            sb.append("</tr>");
        }

        sb.append("</tbody></table>");
        sb.append("<div class='footer'>");
        sb.append("<span>MediaHub HTTP/1.1 Socket Web Server</span>");
        sb.append("<span>Tự Động Duyệt Danh Mục (Auto Directory Indexing)</span>");
        sb.append("</div>");
        sb.append("</div></body></html>");

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        HttpResponse listingRes = new HttpResponse(HttpStatus.OK, new ByteArrayInputStream(bytes), bytes.length);
        listingRes.setHeader("Content-Type", "text/html; charset=UTF-8");
        listingRes.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        return listingRes;
    }
}
