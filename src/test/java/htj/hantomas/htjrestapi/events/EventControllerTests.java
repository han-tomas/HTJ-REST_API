package htj.hantomas.htjrestapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import htj.hantomas.htjrestapi.common.BaseController;
import htj.hantomas.htjrestapi.common.RestDocsConfiguration;
import htj.hantomas.htjrestapi.common.TestDescription;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@WebMvcTest

public class EventControllerTests extends BaseController {

    /*
    @MockBean
    EventRepository eventRepository;
    */
    /*
        @WebMvcTest는 web용 bean들만 등록해주고, Repository bean은 등록해 주지 않는다.
        @MockBean으로 등록해준다.
     */
    @Autowired
    EventRepository eventRepository;

    @Test
    @TestDescription("정삭적으로 이벤트를 생성하는 테스트")
    public void createEvent() throws Exception {
        EventDto event = EventDto.builder()
                        //.id(100) // id는 DB에 들어갈때 자동 생성 되어야 되는 값.
                        .name("Spring")
                        .description("REST API Development with Spring")
                        .beginEnrollmentDateTime(LocalDateTime.of(2024,01,25,14,21))
                        .closeEnrollmentDateTime(LocalDateTime.of(2024,01,26,14,21))
                        .beginEventDateTime(LocalDateTime.of(2024,01,25,14,21))
                        .endEventDateTime(LocalDateTime.of(2024,01,26,14,21))
                        .basePrice(100)
                        .maxPrice(200)
                        .limitOfEnrollment(100)
                        .location("강남역 D2 스타텁 팩토리")
                        //.free(true) //.basePrice(100), .maxPrice(200), .limitOfEnrollment(100) 값이 있는경우에 free는 true 일 수 없다.
                        //.offline(false) // .location("강남역 D2 스타텁 팩토리") 가 있는 경우 offline이 false가 될 수 없다.
                        //.eventStatus(EventStatus.PUBLISHED) //EventStatus는 DRAFT여야 하지만 PUBLISHED로 설정
                        .build();
        /*
            EventRepository는 mock 객체이기 때문에, createEvent에서는 Null값을 반환하면서, NullPointException 발생
            Stubbing 작업이 필요하다.
                만들어진 mock 객체의 메소드를 실행했을 때 어떤 리턴 값을 리턴할지를 정의하는 것
         */
        //event.setId(10);
        /*
        Mockito.when(eventRepository.save(event)).thenReturn(event);
        EventDto를 ModelMapper의 메서드를 통해 만든 새로운 event객체로 eventRepository.save(event)의 event객체가 아니므로
        stubbing하기 전과 같이 null 값이 리턴 되어버린다.
        그래서 더이상 WebMvcTest를 이용한 slice 테스트는 불가.
        */
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                //.andExpect(header().string(HttpHeaders.CONTENT_TYPE,MediaTypes.HAL_JSON_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/hal+json;charset=UTF-8")) // 한글 깨짐 오류 수정
                .andExpect(jsonPath("free").value(false)) // free는 true면 안된다
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name())) //EventStatus는 DRAFT여야 한다.
                /*
                   EventDto의 값을 이용하기 때문에 테스트에 넣어준 id와 free값(DTO에 없는)은 자동으로 무시 되므로, 테스트는 성공하게 된다.
                */
                // HATEOAS
                .andExpect(jsonPath("_links.self").exists()) // self링크가 잘 만들어졌는지 (존재하는지)
                //.andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.query-events").exists()) // 이벤트 목록으로 가는 링크
                .andExpect(jsonPath("_links.update-event").exists()) // 이벤트 수정으로 가는 링크
                .andDo(document("create-event",
                        links( // 링크 정보를 문서화
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-events").description("link to query events"),
                                linkWithRel("update-event").description("link to update an existing event"),
                                linkWithRel("profile").description("link to update an existing event")
                        ),
                        requestHeaders( // 요청 헤더 정보를 문서화
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestFields( // 요청 필드 정보를 문서화 : 요청에 들어오는 각각의 값들이 어떤것을 의미하는지
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("decription of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                                fieldWithPath("endEventDateTime").description("date time of end of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("base price of new event"),
                                fieldWithPath("maxPrice").description("max price of new event"),
                                fieldWithPath("limitOfEnrollment").description("limit of enrollment")


                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content type")
                        ),
                        relaxedResponseFields( // 응답 필드중 일부 정보만 문서화
                                fieldWithPath("id").description("identifier of new event"),
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("decription of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                                fieldWithPath("endEventDateTime").description("date time of end of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("base price of new event"),
                                fieldWithPath("maxPrice").description("max price of new event"),
                                fieldWithPath("limitOfEnrollment").description("limit of enrollment"),
                                fieldWithPath("free").description("it tells if this event is free or not"),
                                fieldWithPath("offline").description("it tells if this event is offline event or not"),
                                fieldWithPath("eventStatus").description("event status"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.query-events.href").description("link to query event list"),
                                fieldWithPath("_links.update-event.href").description("link to update existing event"),
                                fieldWithPath("_links.profile.href").description("link to profile")
                        )
                ))
        ;
    }
    /*
    이 코드는 Spring MVC의 MockMvc를 사용하여 "/api/events" 엔드포인트에 POST 요청을 보내는 테스트입니다.

    1. mockMvc.perform(post("/api/events")...) 부분에서 POST 요청을 보냅니다.
    요청의 Content-Type은 MediaType.APPLICATION_JSON으로, JSON 형식의 데이터를 보내겠다는 의미입니다.
    accept(MediaTypes.HAL_JSON_VALUE)는 이 요청이 HAL JSON 형식의 응답을 받을 수 있음을 나타냅니다.

    2. andDo(print())는 요청/응답을 콘솔에 출력하도록 합니다. 이는 테스트 시 디버깅에 도움이 됩니다.

    3. andExpect(status().isCreated())는 응답 상태 코드가 201(Created)이라는 것을 검증합니다.
       즉, 이 테스트는 새로운 이벤트가 성공적으로 생성되었음을 확인합니다.

    4. andExpect(jsonPath("id").exists());는 응답 본문에 'id'라는 필드가 존재함을 검증합니다.
       jsonPath()는 JSON 응답을 검사하는 데 사용되며, "id"는 JSON 객체의 'id' 필드를 가리킵니다.
       exists() 메서드는 해당 필드가 존재함을 확인합니다.
       이를 통해 이벤트가 성공적으로 생성되면서 새로운 ID가 부여되었음을 확인합니다.

        따라서 이 테스트는 "/api/events" 엔드포인트에 POST 요청을 보내 새 이벤트를 생성하고,
        그 결과로 반환된 응답이 적절한 상태 코드와 새 이벤트의 ID를 포함하고 있는지 검증합니다.
     */
    @Test
    @TestDescription("입력 받을 수 없는 값을 사용한 경우 에러가 발생하는 테스트")
    public void createEvent_Bad_request() throws Exception { // 입력값 이외의 입력에 대한 Bad_Request응답
        /*
        application.properties에 아래의 Jackson의 ObjectMapper 커스터마이징을 통해
        spring.jackson.deserialization.fail-on-unknown-properties=true
        입력값 이외의 properties를 받을 경우 Bad_Request로 응답
         */
        Event event = Event.builder()
                .id(100) // id는 DB에 들어갈때 자동 생성 되어야 되는 값.
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2024,01,25,14,21))
                .closeEnrollmentDateTime(LocalDateTime.of(2024,01,26,14,21))
                .beginEventDateTime(LocalDateTime.of(2024,01,25,14,21))
                .endEventDateTime(LocalDateTime.of(2024,01,26,14,21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .free(true) //.basePrice(100), .maxPrice(200), .limitOfEnrollment(100) 값이 있는경우에 free는 true 일 수 없다.
                .offline(false) // .location("강남역 D2 스타텁 팩토리") 가 있는 경우 offline이 false가 될 수 없다.
                .eventStatus(EventStatus.PUBLISHED) //EventStatus는 DRAFT여야 하지만 PUBLISHED로 설정
                .build();

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @TestDescription("입력값이 비어있는 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request_Empty_Input() throws Exception { // 입력값이 비어있을때 @Valid를 이용한 Validation(검증)
        EventDto eventDto = EventDto.builder().build();

        this.mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력값이 잘못된 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request_Wrong_Input() throws Exception { // 입력값이 잘못 되었을때
                                                                        /*
                                                                             (ex. 이벤트 종료날짜가 시작날짜보다 빠른경우,
                                                                                    basePrice보다 maxPrice가 작은 경우.)
                                                                         */
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2024,01,25,14,21))
                .closeEnrollmentDateTime(LocalDateTime.of(2024,01,24,14,21))
                .beginEventDateTime(LocalDateTime.of(2024,01,24,14,21))
                .endEventDateTime(LocalDateTime.of(2024,01,23,14,21))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .build();

        this.mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].objectName").exists()) // 에러 배열 중 objectName
                //.andExpect(jsonPath("$[0].field").exists())// 어떤 필드에서 발생한 오류인지 // GlobalErrors의 경우에 오류 발생할 수 있으므로
                .andExpect(jsonPath("errors[0].defaultMessage").exists()) // 기본 메세지는 무엇인지
                .andExpect(jsonPath("errors[0].code").exists()) // 에러 코드는 무엇인지
                //.andExpect(jsonPath("$[0].rejectedValue").exists()) // 에러가 발생된 값이 무엇이였는지.// GlobalErrors의 경우에 오류 발생할 수 있으므로 주석처리 하겠다.
                .andExpect(jsonPath("_links.index").exists())
        ;
    }
    @Test
    @TestDescription("30개의 이벤트를 10개씩 두번째 페이지 조회하기")
    public void queryEvents() throws Exception{
        // Given
        IntStream.range(0,30).forEach(this::generatedEvent); // 메서드 표현식 메서드 레퍼런스

        // When & Then
        this.mockMvc.perform(get("/api/events")
                        .param("page","1")
                        .param("size","10")
                        .param("sort","name,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())//_embedded안에 eventList[0]안에 _links.self가 존재하는가
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("query-events")) // profile에 관한 링크를 만드려면 문서화 필요
        ;
    }
    private Event generatedEvent(int index){
        Event event = Event.builder()
                .name("event " + index)
                .description("test index " + index)
                .beginEnrollmentDateTime(LocalDateTime.of(2024,01,25,14,21))
                .closeEnrollmentDateTime(LocalDateTime.of(2024,01,26,14,21))
                .beginEventDateTime(LocalDateTime.of(2024,01,25,14,21))
                .endEventDateTime(LocalDateTime.of(2024,01,26,14,21))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .free(false)
                .offline(true)
                .eventStatus(EventStatus.DRAFT)
                .build();

        return this.eventRepository.save(event);

    }
    @Test
    @TestDescription("기존의 이벤트를 하나 조회하기")
    public void getEvent() throws Exception{
        // Given
        Event event = this.generatedEvent(100);
        // When & Then
        this.mockMvc.perform(get("/api/events/{id}", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("get-event"))
        ;
    }
    @Test
    @TestDescription("없는 이벤트를 조회했을 때 404 응답받기")
    public void getEvent404() throws Exception{
        // When & Then
        this.mockMvc.perform(get("/api/events/1183"))
                .andExpect(status().isNotFound())
        ;
    }
    @Test
    @TestDescription("이벤트를 정상적으로 수정하기")
    public void updateEvent() throws Exception{
        //Given
        Event event = this.generatedEvent(200); // 만들어질때 name은 event 200
        EventDto eventDto = this.modelMapper.map(event, EventDto.class); // ModelMapper를 이용하여 event에 있는 것을 EventDto에 담는다.
        String eventName = "Updated Event";
        eventDto.setName(eventName);

        //When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(eventDto)) // content에 eventDto를 담아준다.
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(eventName))
                .andExpect(jsonPath("_links.self").exists())
                .andDo(document("update-event"))
        ;


    }
    @Test
    @TestDescription("입력값이 비어있는 경우 경우에 이벤트 수정 실패")
    public void updateEvent400_Empty() throws Exception{
        //Given
        Event event = this.generatedEvent(200);
        EventDto eventDto = new EventDto();

        //When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(eventDto))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;


    }
    @Test
    @TestDescription("입력값이 잘못된 경우에 이벤트 수정 실패")
    public void updateEvent400wrong() throws Exception{
        //Given
        Event event = this.generatedEvent(200);
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        eventDto.setBasePrice(20000);
        eventDto.setMaxPrice(1000);

        //When & Then
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(eventDto))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;


    }
    @Test
    @TestDescription("존재하지 않는 이벤트 수정 실패")
    public void updateEvent404() throws Exception{
        //Given
        Event event = this.generatedEvent(200);
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);

        //When & Then
        this.mockMvc.perform(put("/api/events/123123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(eventDto))
                )
                .andDo(print())
                .andExpect(status().isNotFound())
        ;


    }
}
