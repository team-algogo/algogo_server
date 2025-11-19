package com.ssafy.algogo.program.group.controller;

import com.ssafy.algogo.program.group.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
public class GroupController {
  private final GroupService groupService;
}
