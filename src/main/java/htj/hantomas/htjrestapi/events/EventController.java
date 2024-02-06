package htj.hantomas.htjrestapi.events;

import htj.hantomas.htjrestapi.common.ErrorsResource;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Controller
@RequestMapping(value = "/api/events",produces = MediaTypes.HAL_JSON_VALUE)
/*
    클래스 안에 있는 모든 Handler들은 HAL_JSON ContentType으로 요청을 보낼거다.
*/
public class EventController {
    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;
    public EventController(EventRepository eventRepository, ModelMapper modelMapper, EventValidator eventValidator){
        this.eventRepository = eventRepository;
        this.modelMapper = modelMapper;
        this.eventValidator = eventValidator;
    }
    private ResponseEntity badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(new ErrorsResource(errors)); // 에러를 리소스로 변환
    }
    @PostMapping
    /*
    ResourceSupport	-> RepresentationModel
    Resource	    -> EntityModel
    Resources	    -> CollectionModel
    PagedResources	-> PagedModel
     */
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors){
        /*
            @Valid 어노테이션을 사용하면 Request에 있는 값들,
            즉 EventDto(또는 Entity)에 값들을 바인딩 할 때, 검증을 수행 할 수 있고
            검증을 수행한 결과들을 모아 Errors타입에 errors라는 객체에 담는다.
         */
        if(errors.hasErrors()){
            //return ResponseEntity.badRequest().build(); // 에러가 있으면 Bad_Request를 리턴한다.
            //return ResponseEntity.badRequest().body(ErrorsResource.of(errors);// badRequest를 받아서 본문(body)에 넣어주는데 이를 리소스로 변환
            /*
            private ResponseEntity badRequest(Errors errors) { // eventValidator(유효성검사) 후에도 똑같은 코드를 리팩토링
                return ResponseEntity.badRequest().body(ErrorsResource.of(errors));
            }
             */
            return badRequest(errors);
            /*
                하지만 errors의 경우에는 자바 빈 스펙을 준수한 객체가 아니다. 따라서 BeanSerialization을 통해서 JSON변환이 불가능하다.
                즉, return ResponseEntity.badRequest().body(errors);는 에러발생 => Customize한 ErrorsSerializer를 이용해 해결
             */
        }

        eventValidator.validate(eventDto, errors);
        if(errors.hasErrors()){ // eventValidator를 통해 들어온 error가 있으면
            //return ResponseEntity.badRequest().build(); // Bad_Request를 리턴한다.
            //return ResponseEntity.badRequest().body(ErrorsResource.of(errors);// 에러를 리소스로 변환
            return badRequest(errors);
        }

        Event event = modelMapper.map(eventDto, Event.class);
        event.update();
        Event newEvent = this.eventRepository.save(event);

        //======================HATEOAS 링크 추가하는 부분 ============================================
        WebMvcLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());
        URI createdUri = selfLinkBuilder.toUri(); // linkTo : HATEOAS가 제공하는 링크를 만들어주는 기능
        // 매핑된 정보를 읽어 와서 링크를 만드는 방식         // /api/events / id

        EventResource eventResource = new EventResource(newEvent);
        //EntityModel eventResource = EntityModel.of(newEvent); // EntityModel을 사용하는 경우
        eventResource.add(linkTo(EventController.class).withRel("query-events"));// 이벤트 목록으로 가는 링크
        //eventResource.add(selfLinkBuilder.withSelfRel());
        //보통 self링크는 해당 이벤트 리소스 마다 생성해줘야 하기 때문에 EventResource에 추가해 주는 것이 좋다.
        eventResource.add(selfLinkBuilder.withRel("update-event")); // 이벤트 수정으로 가는 링크
        eventResource.add(Link.of("/docs/index.html#resources-events-create").withRel("profile")); // profile로 가는 링크 추가
        //==========================================================================================

        return ResponseEntity.created(createdUri).body(eventResource);
        /*
            Event라는 도메인은 자바 빈 스펙을 준수
            Controller에서 body에 담아준 객체 event를 JSON으로 변환할 때, ObjectMapper를 사용해서 변환을 하는데
            ObjectMapper는 자바 빈 스펙을 준수한 event 객체의 정보를 BeanSerializer를 통해서 JSON으로 변환이 가능하다.
            (아무런 Cutomize된 Serialization없이도)
             Serialization : 어떤 객체를 JSON으로 변환하는 것 <-> Deserialization
         */
    }
    @GetMapping
    public ResponseEntity queryEvents(Pageable pageable, PagedResourcesAssembler<Event> assembler){
        Page<Event> page = this.eventRepository.findAll(pageable);
        //PagedResourcesAssembler<T> 를 사용해서 findAll(pageable) 결과로 나온 page 를 리소스로 바꾸어서 링크 정보를 추가

        //var pagedResources = assembler.toModel(page) // 현재 페이지,이전 페이지,다음 페이지에 대한 링크
        var pagedResources = assembler.toModel(page, e -> new EventResource(e)); // 완전한 HATEOAS 를 충족하기 위해서는 각각의 이벤트(self)로 갈 수 있는 링크

        pagedResources.add(Link.of("/docs/index.html#resources-events-list").withRel("profile")); // profile링크 추가
        return ResponseEntity.ok(pagedResources);
    }

    @GetMapping("/{id}")
    public ResponseEntity getEvent(@PathVariable Integer id){
        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        if(optionalEvent.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Event event = optionalEvent.get();
        EventResource eventResource = new EventResource(event);
        eventResource.add(Link.of("/docs/index.html#resources-events-get").withRel("profile"));
        return ResponseEntity.ok(eventResource);
    }

    //@PostMapping //("/api/events") 위에서 매핑되었기 때문에 중복해서 설정안해도 됨.
    /*
    public ResponseEntity createEvent(@RequestBody EventDto eventDto){
        /*
        Event event = Event.builder()
                .name(eventDto.getName())
                ...
                .build();
         EventDto를 사용하기 위해서는 이렇게 다 정의 후 사용해야 하지만,
         ModelMapper 를 통해 이 과정을 생략할 수 있다.
        */
        /*
        Event event = modelMapper.map(eventDto, Event.class);

        Event newEvent = this.eventRepository.save(event);

        URI createdUri = linkTo(EventController.class).slash(newEvent.getId()).toUri();
        event.setId(10);
        return ResponseEntity.created(createdUri).body(event);
    }
    */
    /*
    public ResponseEntity createEvent(@RequestBody Event event){
        Event newEvent = this.eventRepository.save(event);

        URI createdUri = linkTo(EventController.class).slash(newEvent.getId()).toUri();
        event.setId(10);
        return ResponseEntity.created(createdUri).body(event);
    }
    */

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
