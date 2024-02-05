package htj.hantomas.htjrestapi.common;

import htj.hantomas.htjrestapi.index.IndexController;
import org.springframework.hateoas.EntityModel;
import org.springframework.validation.Errors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ErrorsResource extends EntityModel<Errors> {
    public ErrorsResource(Errors errors){
        super(errors);
        add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
    }
    /*
    public static EntityModel<Errors> of(Errors errors) {
        EntityModel<Errors> errorsModel = EntityModel.of(errors);
        errorsModel.add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
        return errorsModel; // 에러를 리소스로 변환할 때 index에 대한 링크 추가
    }
    */
}