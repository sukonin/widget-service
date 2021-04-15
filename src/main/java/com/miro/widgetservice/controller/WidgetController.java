package com.miro.widgetservice.controller;

import com.miro.widgetservice.dto.WidgetDto;
import com.miro.widgetservice.service.WidgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "api/v1")
@RequiredArgsConstructor
public class WidgetController {

    private final WidgetService widgetService;

    @PostMapping("/widget")
    @ResponseStatus(HttpStatus.CREATED)
    public WidgetDto createWidget(@RequestBody @Valid WidgetDto widgetDto) {
        return widgetService.create(widgetDto);
    }

    @PutMapping("/widget/{id}")
    @ResponseStatus(HttpStatus.OK)
    public WidgetDto updateWidget(@PathVariable Long id, @RequestBody @Valid WidgetDto widgetDto) {
        return widgetService.update(id, widgetDto);
    }

    @GetMapping("/widget/{id}")
    @ResponseStatus(HttpStatus.OK)
    public WidgetDto getWidget(@PathVariable Long id) {
        return widgetService.findById(id);
    }

    @GetMapping("/widget")
    @ResponseStatus(HttpStatus.OK)
    public List<WidgetDto> getAllWidgets() {
        return widgetService.findAll();
    }

    @DeleteMapping("/widget/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWidget(@PathVariable Long id) {
        widgetService.deleteById(id);
    }
}
