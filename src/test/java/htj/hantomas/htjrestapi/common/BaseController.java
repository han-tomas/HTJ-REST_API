package htj.hantomas.htjrestapi.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest // Slicing Test 안하고
@AutoConfigureMockMvc // SpringBootTest시 MockMVC를 사용하려면
/*
Web과 관련된 것만 등록을 해줬기 때문에 Slicing Test라 한다
Web과 관련된 Bean들만 등록해서 만듦 => 더 빠름
구역을 나눠서 테스트한다. => 단위 테스트라고 할 순 없음
 */
@AutoConfigureRestDocs // Spring RestDocs 사용
@Import(RestDocsConfiguration.class)// 포멧팅하는 설정
@ActiveProfiles("test")
@Ignore
public class BaseController {
    @Autowired
    protected MockMvc mockMvc; //protected => 동일 패키지 + 다른 패키지의 하위클래스에서 접근 가능
    /*
    MockMVC를 사용하면 moc으로 만들어져 있는, 모킹 되어있는 Dispatcher Servlet을 상대로
    가짜 요청을 Dispatcher Servlet에게 보내고 응답을 확인할 수 있는 테스트를 만들 수 있다.
    -> Web Server를 띄우지 않기 때문에 빠르다
        하지만, Dispatcher Servlet까지 만들어야 하기 때문에 단위 테스트 보단 오래걸린다.
     */
    @Autowired
    protected ModelMapper modelMapper;
    @Autowired
    protected ObjectMapper objectMapper;
    /*
     SpringBoot를 사용할 때 MappingJacksonJson이 의존성으로 들어가 있으면,
     ObjectMapper를 자동으로 bean으로 등록을 해준다.
     */
}
