package com.miro.widgetservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miro.widgetservice.dto.WidgetReqDto;
import com.miro.widgetservice.dto.WidgetRespDto;
import com.miro.widgetservice.service.WidgetService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.assertj.core.api.BDDAssertions;
import org.assertj.core.api.Condition;
import org.assertj.core.data.Index;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = WidgetServiceApplication.class)
class WidgetServiceIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private WidgetService widgetService;

    protected MockMvc client;

    @BeforeEach
    void setUp() {
        widgetService.deleteAll();

        client = MockMvcBuilders
            .webAppContextSetup(wac)
            .alwaysDo(MockMvcResultHandlers.print())
            .build();
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("zIndexGenerator")
    void create_CreateWidget_Success(Integer zIndex) {
        List<WidgetReqDto> initWidgetList = initData();
        widgetService.saveAll(initWidgetList);

        //given
        WidgetReqDto widget = WidgetReqDto.builder()
            .height(50)
            .width(50)
            .xPoint(5)
            .yPoint(5)
            .zIndex(zIndex)
            .build();

        //when
        String response = client
            .perform(post("/api/v1/widget")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(widget)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        WidgetRespDto savedWidget = objectMapper.readValue(response, WidgetRespDto.class);

        //then
        List<WidgetRespDto> widgets = widgetService.findAll();

        Set<Integer> uniqueZIndex = widgets.stream()
            .map(WidgetRespDto::getZIndex)
            .collect(Collectors.toCollection(HashSet::new));

        BDDAssertions.assertThat(widgets)
            .hasSize(initWidgetList.size() + 1)
            .contains(savedWidget)
            .size()
            .isEqualTo(uniqueZIndex.size());
    }

    @Test
    @SneakyThrows
    void create_CreateWidgetWithMaxZIndexAlreadyExist_ThrowException() {
        widgetService.create(getWidget(Integer.MAX_VALUE));

        //given
        WidgetReqDto widget = WidgetReqDto.builder()
            .height(50)
            .width(50)
            .xPoint(5)
            .yPoint(5)
            .zIndex(Integer.MAX_VALUE)
            .build();

        //when
        client
            .perform(post("/api/v1/widget")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(widget)))
            .andExpect(status().isBadRequest());

        //then
        List<WidgetRespDto> widgets = widgetService.findAll();

        BDDAssertions.assertThat(widgets)
            .hasSize(1);
    }

    @Test
    @SneakyThrows
    void create_CreateWidgetWithLessThenMaxZIndex_Success() {
        widgetService.create(getWidget(Integer.MAX_VALUE));

        //given
        WidgetReqDto widget = WidgetReqDto.builder()
            .height(50)
            .width(50)
            .xPoint(5)
            .yPoint(5)
            .zIndex(Integer.MAX_VALUE - 1)
            .build();

        //when
        String response = client
            .perform(post("/api/v1/widget")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(widget)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        WidgetRespDto savedWidget = objectMapper.readValue(response, WidgetRespDto.class);

        //then
        List<WidgetRespDto> widgets = widgetService.findAll();

        Condition<Integer> i1Condition = new Condition<>(i -> i.equals(Integer.MAX_VALUE), "index");
        Condition<Integer> i2Condition = new Condition<>(i -> i.equals(Integer.MAX_VALUE - 1), "index");

        BDDAssertions.assertThat(widgets)
            .hasSize(2)
            .contains(savedWidget)
            .extracting(WidgetRespDto::getZIndex)
            .has(i1Condition, Index.atIndex(1))
            .has(i2Condition, Index.atIndex(0));
    }

    @Test
    @SneakyThrows
    void create_CreateWidgetWithLessThenMinZIndex_ShouldShift() {
        widgetService.create(getWidget(Integer.MIN_VALUE));

        //given
        WidgetReqDto widget = WidgetReqDto.builder()
            .height(50)
            .width(50)
            .xPoint(5)
            .yPoint(5)
            .zIndex(Integer.MIN_VALUE)
            .build();

        //when
        String response = client
            .perform(post("/api/v1/widget")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(widget)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        WidgetRespDto savedWidget = objectMapper.readValue(response, WidgetRespDto.class);

        //then
        List<WidgetRespDto> widgets = widgetService.findAll();

        Condition<Integer> i1Condition = new Condition<>(i -> i.equals(Integer.MIN_VALUE), "index");
        Condition<Integer> i2Condition = new Condition<>(i -> i.equals(Integer.MIN_VALUE + 1), "index");

        BDDAssertions.assertThat(widgets)
            .hasSize(2)
            .contains(savedWidget)
            .extracting(WidgetRespDto::getZIndex)
            .has(i1Condition, Index.atIndex(0))
            .has(i2Condition, Index.atIndex(1));
    }

    @Test
    @SneakyThrows
    void create_CreateWidgetWithNullZIndexAndEmptyStore_Success() {
        //given
        WidgetReqDto widget = WidgetReqDto.builder()
            .height(50)
            .width(50)
            .xPoint(5)
            .yPoint(5)
            .build();

        //when
        String response = client
            .perform(post("/api/v1/widget")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(widget)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        WidgetRespDto savedWidget = objectMapper.readValue(response, WidgetRespDto.class);

        //then
        List<WidgetRespDto> widgets = widgetService.findAll();

        Condition<Integer> i2Condition = new Condition<>(i -> i.equals(0), "index");

        BDDAssertions.assertThat(widgets)
            .hasSize(1)
            .contains(savedWidget)
            .extracting(WidgetRespDto::getZIndex)
            .has(i2Condition, Index.atIndex(0));
    }

    @Test
    @SneakyThrows
    void create_CreateWidgetWithNullZIndexAndZIndexMaxInStore_ThrowException() {
        widgetService.create(getWidget(Integer.MAX_VALUE));

        //given
        WidgetReqDto widget = WidgetReqDto.builder()
            .height(50)
            .width(50)
            .xPoint(5)
            .yPoint(5)
            .build();

        //when
        client
            .perform(post("/api/v1/widget")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(widget)))
            .andExpect(status().isBadRequest());

        //then
        List<WidgetRespDto> widgets = widgetService.findAll();

        BDDAssertions.assertThat(widgets)
            .hasSize(1);
    }

    @Test
    @SneakyThrows
    void findById_ExistInDatabase_Success() {
        WidgetRespDto savedWidget = widgetService.create(getWidget(1));

        //when
        String body = client
            .perform(get("/api/v1/widget/" + savedWidget.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        WidgetRespDto widget = objectMapper.readValue(body, WidgetRespDto.class);

        //then
        BDDAssertions.assertThat(savedWidget).isEqualTo(widget);
    }

    @Test
    @SneakyThrows
    void findById_DoesNotExistInDatabase_ThrowException() {
        WidgetRespDto savedWidget = widgetService.create(getWidget(1));
        long notExistId = savedWidget.getId() + 1;

        client
            .perform(get("/api/v1/widget/" + notExistId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void deleteById_ExistIdDatabase_Success() {
        WidgetRespDto savedWidget = widgetService.create(getWidget(1));

        client
            .perform(delete("/api/v1/widget/" + savedWidget.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        BDDAssertions.assertThat(widgetService.findAll()).isEmpty();
    }

    @Test
    @SneakyThrows
    void deleteById_DoesNotExistInDatabase_ThrowException() {
        WidgetRespDto savedWidget = widgetService.create(getWidget(1));

        client
            .perform(delete("/api/v1/widget/" + savedWidget.getId() + 1)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        BDDAssertions.assertThat(widgetService.findAll()).hasSize(1);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("zIndexGenerator")
    void update_UpdateWidget_Success(Integer zIndex) {
        List<WidgetReqDto> initWidgetList = initData();
        WidgetRespDto storedWidget = widgetService.saveAll(initWidgetList).get(0);

        //given
        WidgetReqDto widgetForUpdate = WidgetReqDto.builder()
            .height(50)
            .width(50)
            .xPoint(5)
            .yPoint(5)
            .zIndex(zIndex)
            .build();

        //when
        String response = client
            .perform(put("/api/v1/widget/" + storedWidget.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(widgetForUpdate)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        WidgetRespDto savedWidget = objectMapper.readValue(response, WidgetRespDto.class);

        //then
        List<WidgetRespDto> widgets = widgetService.findAll();

        Set<Integer> uniqueZIndex = widgets.stream()
            .map(WidgetRespDto::getZIndex)
            .collect(Collectors.toCollection(HashSet::new));

        BDDAssertions.assertThat(widgets)
            .hasSize(initWidgetList.size())
            .contains(savedWidget)
            .size()
            .isEqualTo(uniqueZIndex.size());
    }

    @SneakyThrows
    @Test
    void update_UpdateWidgetIfMaxZIndexExist_ThrowException() {
        List<WidgetReqDto> initWidgetList = initData();
        WidgetRespDto storedWidget = widgetService.saveAll(initWidgetList).get(0);
        widgetService.create(getWidget(Integer.MAX_VALUE));

        //given
        WidgetReqDto widgetForUpdate = WidgetReqDto.builder()
            .height(50)
            .width(50)
            .xPoint(5)
            .yPoint(5)
            .zIndex(Integer.MAX_VALUE)
            .build();

        //when
        client
            .perform(put("/api/v1/widget/" + storedWidget.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(widgetForUpdate)))
            .andExpect(status().isBadRequest());

        //then
        List<WidgetRespDto> widgets = widgetService.findAll();

        BDDAssertions.assertThat(widgets)
            .isNotEmpty();

        BDDAssertions.assertThat(widgetForUpdate.getZIndex())
            .isNotEqualTo(storedWidget.getZIndex());
    }

    @SneakyThrows
    @Test
    void update_UpdateWidgetDoesNotExist_ThrowException() {
        List<WidgetReqDto> initWidgetList = initData();
        WidgetRespDto storedWidget = widgetService.saveAll(initWidgetList).get(initWidgetList.size() - 1);
        long notExistId = storedWidget.getId() + 1;

        //given
        WidgetReqDto widgetForUpdate = WidgetReqDto.builder()
            .height(50)
            .width(50)
            .xPoint(5)
            .yPoint(5)
            .zIndex(0)
            .build();

        //when
        client
            .perform(put("/api/v1/widget/" + notExistId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(widgetForUpdate)))
            .andExpect(status().isBadRequest());

        //then
        List<WidgetRespDto> widgets = widgetService.findAll();

        BDDAssertions.assertThat(widgets)
            .isNotEmpty()
            .extracting(WidgetRespDto::getId)
            .doesNotContain(notExistId);
    }

    @SneakyThrows
    @Test
    void update_UpdateWidgetWithoutZIndexWhenItForeground_Success() {
        List<WidgetReqDto> initWidgetList = initData();
        WidgetRespDto foregroundWidget = widgetService.saveAll(initWidgetList).get(initWidgetList.size() - 1);

        //given
        WidgetReqDto widgetForUpdate = WidgetReqDto.builder()
            .height(50)
            .width(50)
            .xPoint(5)
            .yPoint(5)
            .build();

        //when
        String response = client
            .perform(put("/api/v1/widget/" + foregroundWidget.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(widgetForUpdate)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        WidgetRespDto savedWidget = objectMapper.readValue(response, WidgetRespDto.class);

        //then
        List<WidgetRespDto> widgets = widgetService.findAll();

        Set<Integer> uniqueZIndex = widgets.stream()
            .map(WidgetRespDto::getZIndex)
            .collect(Collectors.toCollection(HashSet::new));

        BDDAssertions.assertThat(widgets)
            .isNotEmpty()
            .contains(savedWidget)
            .size()
            .isEqualTo(uniqueZIndex.size());

        BDDAssertions.assertThat(foregroundWidget.getZIndex())
            .isEqualTo(savedWidget.getZIndex());
    }

    @SneakyThrows
    @Test
    void findAll_WithoutPagging_Success() {
        List<WidgetReqDto> initWidgetList = initData();
        List<WidgetRespDto> storedWidgets = widgetService.saveAll(initWidgetList);

        //when
        String response = client
            .perform(get("/api/v1/widget/")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        List<WidgetRespDto> allWidgets = objectMapper.readValue(response, new TypeReference<>() {
        });

        //then
        BDDAssertions.assertThat(storedWidgets)
            .isNotEmpty()
            .usingRecursiveFieldByFieldElementComparator()
            .isEqualTo(allWidgets);
    }

    @SneakyThrows
    @Test
    void findAll_WithoutPaggingAndStoreIsEmpty_Success() {

        //when
        String response = client
            .perform(get("/api/v1/widget/")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        List<WidgetRespDto> allWidgets = objectMapper.readValue(response, new TypeReference<>() {
        });

        //then
        BDDAssertions.assertThat(allWidgets)
            .isEmpty();
    }

    @SneakyThrows
    @Test
    void findAll_Pagging_Success() {
        List<WidgetReqDto> initWidgetList = initData();
        widgetService.saveAll(initWidgetList);

        //when
        String response = client
            .perform(get("/api/v1/widget/")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .param("page", "1"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        List<WidgetRespDto> allWidgets = objectMapper.readValue(response, new TypeReference<>() {
        });

        //then
        BDDAssertions.assertThat(allWidgets)
            .isNotEmpty()
            .hasSize(10);
    }

    private List<WidgetReqDto> initData() {
        return IntStream.range(-100, 100)
            .mapToObj(this::getWidget)
            .collect(Collectors.toList());
    }

    private WidgetReqDto getWidget(int zIndex) {
        return WidgetReqDto.builder()
            .height(100)
            .width(200)
            .xPoint(5)
            .yPoint(5)
            .zIndex(zIndex)
            .build();
    }

    static Stream<Integer> zIndexGenerator() {
        return Stream.of(0, 1, -1, 50, -50, 100, 101, -100, -101, 150, -150, Integer.MAX_VALUE, Integer.MIN_VALUE, null);
    }
}