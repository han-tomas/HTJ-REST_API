package htj.hantomas.htjrestapi.events;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class EventResource extends EntityModel<Event> {

    public EventResource(Event event, Iterable<Link> links) {
        super(event, links);
        add(linkTo(EventController.class).slash(event.getId()).withSelfRel());
        //보통 self링크는 해당 이벤트 리소스 마다 생성해줘야 하기 때문에 EventResource에 추가해 주는 것이 좋다.
    }
}
/*
public class EventResource extends RepresentationModel { // RepresentationModel을 상속받음으로써 링크를 담을 수 있다.
    @JsonUnwrapped // DTO를 json형태로 만들었을 때 복잡도가 높을 때 이를 해결하기 위해 사용
                    // 객체 내부의 변수를 직렬화
    private Event event;

    public EventResource(Event event){
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

}
 */
