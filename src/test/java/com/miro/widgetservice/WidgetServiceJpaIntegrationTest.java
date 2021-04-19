/*
package com.miro.widgetservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miro.widgetservice.model.Widget;
import com.miro.widgetservice.model.WidgetEntity;
import com.miro.widgetservice.repository.jpa.WidgetJpaRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("database")
@SpringBootTest(classes = WidgetServiceApplication.class)
class WidgetServiceJpaIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private WidgetJpaRepository widgetRepository;

    protected MockMvc client;

    @BeforeEach
    void setUp() {
        widgetRepository.deleteAll();

        client = MockMvcBuilders
            .webAppContextSetup(wac)
            .alwaysDo(MockMvcResultHandlers.print())
            .build();
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("zIndexGenerator")
    void create_CreateWidget_Success(Integer zIndex) {
        List<WidgetEntity> initWidgetList = initData();
        widgetRepository.saveAll(initWidgetList);

        //given
        Widget widget = Widget.builder()
            .height(50)
            .width(50)
            .xPoint(5f)
            .yPoint(5f)
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

        WidgetEntity savedWidget = objectMapper.readValue(response, WidgetEntity.class);

        //then
        List<WidgetEntity> widgets = widgetRepository.findAll();

        HashSet<Integer> uniqueZIndex = widgets.stream()
            .map(WidgetEntity::getZIndex)
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
        widgetRepository.save(getWidget(Integer.MAX_VALUE));

        //given
        Widget widget = Widget.builder()
            .height(50)
            .width(50)
            .xPoint(5f)
            .yPoint(5f)
            .zIndex(Integer.MAX_VALUE)
            .build();

        //when
        client
            .perform(post("/api/v1/widget")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(widget)))
            .andExpect(status().isBadRequest());

        //then
        List<WidgetEntity> widgets = widgetRepository.findAll();

        BDDAssertions.assertThat(widgets)
            .hasSize(1);
    }

    @Test
    @SneakyThrows
    void create_CreateWidgetWithLessThenMaxZIndex_Success() {
        widgetRepository.save(getWidget(Integer.MAX_VALUE));

        //given
        Widget widget = Widget.builder()
            .height(50)
            .width(50)
            .xPoint(5f)
            .yPoint(5f)
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

        WidgetEntity savedWidget = objectMapper.readValue(response, WidgetEntity.class);

        //then
        List<WidgetEntity> widgets = widgetRepository.findAll();
        widgets.sort(Comparator.comparing(WidgetEntity::getZIndex));

        Condition<Integer> i1Condition = new Condition<>(i -> i.equals(Integer.MAX_VALUE), "index");
        Condition<Integer> i2Condition = new Condition<>(i -> i.equals(Integer.MAX_VALUE - 1), "index");

        BDDAssertions.assertThat(widgets)
            .hasSize(2)
            .contains(savedWidget)
            .extracting(WidgetEntity::getZIndex)
            .has(i1Condition, Index.atIndex(1))
            .has(i2Condition, Index.atIndex(0));
    }

    @Test
    @SneakyThrows
    void create_CreateWidgetWithLessThenMinZIndex_ShouldShift() {
        widgetRepository.save(getWidget(Integer.MIN_VALUE));

        //given
        Widget widget = Widget.builder()
            .height(50)
            .width(50)
            .xPoint(5f)
            .yPoint(5f)
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

        WidgetEntity savedWidget = objectMapper.readValue(response, WidgetEntity.class);

        //then
        List<WidgetEntity> widgets = widgetRepository.findAll();
        widgets.sort(Comparator.comparing(WidgetEntity::getZIndex));

        Condition<Integer> i1Condition = new Condition<>(i -> i.equals(Integer.MIN_VALUE), "index");
        Condition<Integer> i2Condition = new Condition<>(i -> i.equals(Integer.MIN_VALUE + 1), "index");

        BDDAssertions.assertThat(widgets)
            .hasSize(2)
            .contains(savedWidget)
            .extracting(WidgetEntity::getZIndex)
            .has(i1Condition, Index.atIndex(0))
            .has(i2Condition, Index.atIndex(1));
    }

    @Test
    @SneakyThrows
    void create_CreateWidgetWithNullZIndexAndEmptyStore_Success() {
        //given
        Widget widget = Widget.builder()
            .height(50)
            .width(50)
            .xPoint(5f)
            .yPoint(5f)
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

        WidgetEntity savedWidget = objectMapper.readValue(response, WidgetEntity.class);

        //then
        List<WidgetEntity> widgets = widgetRepository.findAll();

        Condition<Integer> i2Condition = new Condition<>(i -> i.equals(0), "index");

        BDDAssertions.assertThat(widgets)
            .hasSize(1)
            .contains(savedWidget)
            .extracting(WidgetEntity::getZIndex)
            .has(i2Condition, Index.atIndex(0));
    }

    @Test
    @SneakyThrows
    void create_CreateWidgetWithNullZIndexAndZIndexMaxInStore_ThrowException() {
        widgetRepository.save(getWidget(Integer.MAX_VALUE));

        //given
        Widget widget = Widget.builder()
            .height(50)
            .width(50)
            .xPoint(5f)
            .yPoint(5f)
            .build();

        //when
        client
            .perform(post("/api/v1/widget")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(widget)))
            .andExpect(status().isBadRequest());

        //then
        List<WidgetEntity> widgets = widgetRepository.findAll();

        BDDAssertions.assertThat(widgets)
            .hasSize(1);
    }

    @Test
    @SneakyThrows
    void findById_ExistInDatabase_Success() {
        WidgetEntity savedWidget = widgetRepository.save(getWidget(1));

        //when
        String body = client
            .perform(get("/api/v1/widget/" + savedWidget.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        WidgetEntity widget = objectMapper.readValue(body, WidgetEntity.class);

        //then
        BDDAssertions.assertThat(savedWidget).isEqualTo(widget);
    }

    @Test
    @SneakyThrows
    void findById_DoesNotExistInDatabase_ThrowException() {
        WidgetEntity savedWidget = widgetRepository.save(getWidget(1));
        long notExistId = savedWidget.getId() + 1;

        client
            .perform(get("/api/v1/widget/" + notExistId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void deleteById_ExistIdDatabase_Success() {
        WidgetEntity savedWidget = widgetRepository.save(getWidget(1));

        client
            .perform(delete("/api/v1/widget/" + savedWidget.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        BDDAssertions.assertThat(widgetRepository.findAll()).isEmpty();
    }

    @Test
    @SneakyThrows
    void deleteById_DoesNotExistInDatabase_ThrowException() {
        WidgetEntity savedWidget = widgetRepository.save(getWidget(1));

        client
            .perform(delete("/api/v1/widget/" + savedWidget.getId() + 1)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        BDDAssertions.assertThat(widgetRepository.findAll()).hasSize(1);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("zIndexGenerator")
    void update_UpdateWidget_Success(Integer zIndex) {
        List<WidgetEntity> initWidgetList = initData();
        WidgetEntity storedWidget = widgetRepository.saveAll(initWidgetList).get(0);

        //given
        Widget widgetForUpdate = Widget.builder()
            .id(storedWidget.getId())
            .height(50)
            .width(50)
            .xPoint(5f)
            .yPoint(5f)
            .zIndex(zIndex)
            .build();

        //when
        String response = client
            .perform(put("/api/v1/widget/" + widgetForUpdate.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(widgetForUpdate)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        WidgetEntity savedWidget = objectMapper.readValue(response, WidgetEntity.class);

        //then
        List<WidgetEntity> widgets = widgetRepository.findAll();

        HashSet<Integer> uniqueZIndex = widgets.stream()
            .map(WidgetEntity::getZIndex)
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
        List<WidgetEntity> initWidgetList = initData();
        WidgetEntity storedWidget = widgetRepository.saveAll(initWidgetList).get(0);
        widgetRepository.save(getWidget(Integer.MAX_VALUE));

        //given
        Widget widgetForUpdate = Widget.builder()
            .id(storedWidget.getId())
            .height(50)
            .width(50)
            .xPoint(5f)
            .yPoint(5f)
            .zIndex(Integer.MAX_VALUE)
            .build();

        //when
        client
            .perform(put("/api/v1/widget/" + widgetForUpdate.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(widgetForUpdate)))
            .andExpect(status().isBadRequest());

        //then
        List<WidgetEntity> widgets = widgetRepository.findAll();

        BDDAssertions.assertThat(widgets)
            .isNotEmpty();

        BDDAssertions.assertThat(widgetForUpdate.getZIndex())
            .isNotEqualTo(storedWidget.getZIndex());
    }

    @SneakyThrows
    @Test
    void update_UpdateWidgetDoesNotExist_ThrowException() {
        List<WidgetEntity> initWidgetList = initData();
        WidgetEntity storedWidget = widgetRepository.saveAll(initWidgetList).get(0);

        //given
        WidgetEntity widgetForUpdate = WidgetEntity.builder()
            .id(100000000000L)
            .height(50)
            .width(50)
            .xPoint(5)
            .yPoint(5)
            .zIndex(0)
            .build();

        //when
        client
            .perform(put("/api/v1/widget/" + widgetForUpdate.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(widgetForUpdate)))
            .andExpect(status().isBadRequest());

        //then
        List<WidgetEntity> widgets = widgetRepository.findAll();

        BDDAssertions.assertThat(widgets)
            .isNotEmpty()
            .doesNotContain(widgetForUpdate);
    }

    @SneakyThrows
    @Test
    void update_UpdateWidgetWithoutZIndexWhenItForeground_Success() {
        List<WidgetEntity> initWidgetList = initData();
        WidgetEntity foregroundWidget = widgetRepository.saveAll(initWidgetList).get(initWidgetList.size() - 1);

        //given
        WidgetEntity widgetForUpdate = WidgetEntity.builder()
            .id(foregroundWidget.getId())
            .height(50)
            .width(50)
            .xPoint(5)
            .yPoint(5)
            .build();

        //when
        String response = client
            .perform(put("/api/v1/widget/" + widgetForUpdate.getId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(widgetForUpdate)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        WidgetEntity savedWidget = objectMapper.readValue(response, WidgetEntity.class);

        //then
        List<WidgetEntity> widgets = widgetRepository.findAll();

        BDDAssertions.assertThat(widgets)
            .isNotEmpty()
            .contains(widgetForUpdate)
            .contains(savedWidget);

        BDDAssertions.assertThat(foregroundWidget.getZIndex())
            .isEqualTo(savedWidget.getZIndex());
    }

    @SneakyThrows
    @Test
    void findAll_WithoutPagging_Success() {
        List<WidgetEntity> initWidgetList = initData();
        List<WidgetEntity> storedWidgets = widgetRepository.saveAll(initWidgetList);

        //when
        String response = client
            .perform(get("/api/v1/widget/")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        List<WidgetEntity> allWidgets = objectMapper.readValue(response, new TypeReference<>() {
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

        List<WidgetEntity> allWidgets = objectMapper.readValue(response, new TypeReference<>() {
        });

        //then
        BDDAssertions.assertThat(allWidgets)
            .isEmpty();
    }

    @SneakyThrows
    @Test
    void findAll_Pagging_Success() {
        List<WidgetEntity> initWidgetList = initData();
        widgetRepository.saveAll(initWidgetList);

        //when
        String response = client
            .perform(get("/api/v1/widget/")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .param("page", "1"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        List<WidgetEntity> allWidgets = objectMapper.readValue(response, new TypeReference<>() {
        });

        //then
        BDDAssertions.assertThat(allWidgets)
            .isNotEmpty()
            .hasSize(10);
    }

    private List<WidgetEntity> initData() {
        return IntStream.range(-100, 100)
            .mapToObj(this::getWidget)
            .collect(Collectors.toList());
    }

    private WidgetEntity getWidget(int zIndex) {
        return WidgetEntity.builder()
            .height(100)
            .width(200)
            .xPoint(1)
            .yPoint(1)
            .zIndex(zIndex)
            .modificationDate(LocalDateTime.now())
            .build();
    }

    static Stream<Integer> zIndexGenerator() {
        return Stream.of(0, 1, -1, 50, -50, 100, 101, -100, -101, 150, -150, Integer.MAX_VALUE, Integer.MIN_VALUE, null);
    }
}*/
