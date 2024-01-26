package htj.hantomas.htjrestapi.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Controller
@RequestMapping(value = "/api/events",produces = MediaTypes.HAL_JSON_VALUE)
/*
    클래스 안에 있는 모든 Handler들은 HAL_JSON ContentType으로 요청을 보낼거다.
*/
public class EventController {
    @Autowired
    EventRepository eventRepository;
    @PostMapping //("/api/events") 위에서 매핑되었기 때문에 중복해서 설정안해도 됨.
    /*
    클래스 안에 있는 모든 Handler들은 HAL_JSON ContentType으로 요청을 보낼거고
     */
    public ResponseEntity createEvent(@RequestBody Event event){
        Event newEvent = this.eventRepository.save(event);

        URI createdUri = linkTo(EventController.class).slash(newEvent.getId()).toUri();
        event.setId(10);
        return ResponseEntity.created(createdUri).body(event);
    }
    /*
    public ResponseEntity createEvent(@RequestBody Event event){

        URI createdUri = linkTo(EventController.class).slash("{id}").toUri();
        event.setId(10);
        return ResponseEntity.created(createdUri).body(event);
    }
    */

    /*
    public ResponseEntity createEvent(@RequestBody Event event){
        URI createdUri = linkTo(methodOn(EventController.class).createEvent()).slash("{id}").toUri();
        return ResponseEntity.created(createdUri).build();
    }
     */

    /*
    이 코드는 REST API를 설계할 때 많이 사용하는 HATEOAS(Hypertext As The Engine Of Application State) 원칙을 따르는 방법을 보여줍니다.

        1. linkTo(methodOn(EventController.class).createEvent()).slash("{id}") 부분은
            EventController 클래스의 createEvent() 메서드를 호출하는 URL을 생성합니다.
            그리고 이 URL의 끝에 "/{id}"를 추가합니다.
            이렇게 생성된 URL은 새로 생성된 이벤트의 위치를 가리키는데 사용됩니다.

        2. toUri() 메서드는 URL을 java.net.URI 객체로 변환합니다.
            HTTP 응답 헤더에 위치를 나타내는 Location 필드에 사용될 수 있는 형태입니다.

        3. ResponseEntity.created(createdUri).build() 부분은 HTTP 응답을 생성합니다.

    ResponseEntity.created() 메서드는 HTTP 201 Created 상태 코드를 가진 응답을 생성하며, Location 헤더에는 createdUri가 들어갑니다.
     이는 새로 생성된 리소스의 위치를 클라이언트에게 알려주는 역할을 합니다. build() 메서드는 실제 ResponseEntity 객체를 생성합니다.
    따라서 이 코드는 새로운 이벤트를 생성한 후, 그 이벤트의 위치를 Location 헤더에 담아 HTTP 201 Created 응답을 반환하는 역할을 합니다.
    이는 REST API에서 리소스 생성 후에 권장되는 방식입니다.
     */
}
