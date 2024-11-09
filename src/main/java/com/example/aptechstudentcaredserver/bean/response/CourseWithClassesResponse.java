package com.example.aptechstudentcaredserver.bean.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseWithClassesResponse {
    private int id;
    private String className;
    private String courseName;
    private String courseCode;
    private String courseCompTime;
    private Map<String, List<String>> semesters;
}
