package com.miro.widgetservice.controller;

import com.miro.widgetservice.dto.SearchAreaDto;
import com.miro.widgetservice.dto.WidgetReqDto;
import com.miro.widgetservice.dto.WidgetRespDto;
import com.miro.widgetservice.service.WidgetService;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/v1")
@RequiredArgsConstructor
public class WidgetController {

    private final WidgetService widgetService;

    @PostMapping("/widget")
    @ResponseStatus(HttpStatus.CREATED)
    public WidgetRespDto createWidget(@RequestBody @Valid WidgetReqDto widgetReqDto) {
        return widgetService.create(widgetReqDto);
    }

    @PutMapping("/widget/{id}")
    @ResponseStatus(HttpStatus.OK)
    public WidgetRespDto updateWidget(@PathVariable Long id, @RequestBody @Valid WidgetReqDto widgetReqDto) {
        return widgetService.update(id, widgetReqDto);
    }

    @GetMapping("/widget/{id}")
    @ResponseStatus(HttpStatus.OK)
    public WidgetRespDto getWidget(@PathVariable Long id) {
        return widgetService.findById(id);
    }

    @GetMapping("/widget")
    @ResponseStatus(HttpStatus.OK)
    public List<WidgetRespDto> getAllWidgets(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size,
        @RequestParam(required = false) Integer xPoint1,
        @RequestParam(required = false) Integer yPoint1,
        @RequestParam(required = false) Integer xPoint2,
        @RequestParam(required = false) Integer yPoint2) {

        SearchAreaDto searchAreaDto = SearchAreaDto.builder()
            .xPoint1(xPoint1)
            .yPoint1(yPoint1)
            .xPoint2(xPoint2)
            .yPoint2(yPoint2)
            .build();

        return widgetService.findAll(page, size, searchAreaDto);
    }

    @DeleteMapping("/widget/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWidget(@PathVariable Long id) {
        widgetService.deleteById(id);
    }
}
