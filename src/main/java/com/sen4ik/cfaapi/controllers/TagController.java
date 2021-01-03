package com.sen4ik.cfaapi.controllers;

import com.sen4ik.cfaapi.base.Constants;
import com.sen4ik.cfaapi.base.ResponseHelper;
import com.sen4ik.cfaapi.repositories.TagsRepository;
import com.sen4ik.cfaapi.exceptions.RecordNotFoundException;
import com.sen4ik.cfaapi.entities.Tag;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Controller
@RequestMapping(path = Constants.API_PREFIX + "/tag")
@Api(tags = "Tags", description = " ")
public class TagController {

    @Autowired
    private TagsRepository tagsRepository;

    @GetMapping(path="/all")
    @ApiOperation(value = "Get all tags")
    public @ResponseBody Iterable<Tag> getAllTags() {
        return tagsRepository.findAll();
    }

    @GetMapping(path="/{id}")
    @ApiOperation(value = "Get single tag by id")
    public @ResponseBody
    Tag getOne(@PathVariable("id") int id) { // @PathVariable String id
        return tagsRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException(id));
    }

    @PostMapping(path="/add", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Add tag")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    Tag createOne(@RequestBody Tag tag) {
        tagsRepository.save(tag);
        return tag;
    }

    @DeleteMapping(path="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete tag")
    public @ResponseBody ResponseEntity<String> deleteOne(@PathVariable int id) {
        try {
            tagsRepository.deleteById(id);
            return ResponseHelper.deleteSuccess(id);
        } catch (Exception e) {
            return ResponseHelper.deleteFailed(id, e);
        }

    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Update tag")
    public ResponseEntity<Object> update(@RequestBody Tag tag, @PathVariable int id) {
        Optional<Tag> tagOptional = tagsRepository.findById(id);
        if (!tagOptional.isPresent())
            return ResponseEntity.notFound().build();
        tag.setId(id);
        tagsRepository.save(tag);
        tagOptional = tagsRepository.findById(id);
        return new ResponseEntity<>(tagOptional, HttpStatus.OK);
    }

}
