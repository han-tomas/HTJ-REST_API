package htj.hantomas.htjrestapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
/*
Web과 관련된 것만 등록을 해줬기 때문에 Slicing Test라 한다
Web과 관련된 Bean들만 등록해서 만듦 => 더 빠름
구역을 나눠서 테스트한다. => 단위 테스트라고 할 순 없음
 */
public class EventControllerTests {
    @Autowired
    MockMvc mockMvc;
    /*
    MocMVC를 사용하면 moc으로 만들어져 있는, 모킹 되어있는 Dispatcher Servlet을 상대로
    가짜 요청을 Dispatcher Servlet에게 보내고 응답을 확인할 수 있는 테스트를 만들 수 있다.
    -> Web Server를 띄우지 않기 때문에 빠르다
        하지만, Dispatcher Servlet까지 만들어야 하기 때문에 단위 테스트 보단 오래걸린다.
     */
    @Autowired
    ObjectMapper objectMapper;
    /*
     SpringBoot를 사용할 때 MappingJacksonJson이 의존성으로 들어가 있으면,
     ObjectMapper를 자동으로 bean으로 등록을 해준다.
     */
    @Test
    public void createEvent() throws Exception {
        Event event = Event.builder()
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
                        .build();

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists());
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

}
