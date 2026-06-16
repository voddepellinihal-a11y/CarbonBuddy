package com.carbonbuddy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller that forwards SPA routes to index.html for client-side routing.
 */
@Controller
public class SpaController {

    /**
     * Forwards non-file SPA routes to index.html.
     *
     * @return the forward directive to index.html
     */
    @RequestMapping(value = {"/", "/{path:[^\\.]*}", "/{path:[^\\.]*}/{subpath:[^\\.]*}"})
    public String forward() {
        return "forward:/index.html";
    }
}
