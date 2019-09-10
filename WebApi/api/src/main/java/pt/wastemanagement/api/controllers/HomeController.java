package pt.wastemanagement.api.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.wastemanagement.api.views.output.json_home.JsonHomeOutput;

@RestController
public class HomeController {


    public static final String HOME_PATH = "/";

    @GetMapping(value = HOME_PATH, produces = "application/json-home")
    public JsonHomeOutput getHome () {
        return new JsonHomeOutput();
    }
}
