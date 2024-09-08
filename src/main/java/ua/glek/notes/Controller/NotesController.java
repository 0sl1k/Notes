package ua.glek.notes.Controller;

import Utils.CryptoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.glek.notes.Model.Notes;
import ua.glek.notes.Repository.NotesRepo;
import ua.glek.notes.Service.EncryptedDataService;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NotesController {
    @Autowired
    private NotesRepo notesRepo;

    @PostMapping("/save")
    public ResponseEntity<Notes> saveNotes(@RequestBody Notes notes) throws Exception {

        notesRepo.save(notes);
        return new ResponseEntity<>(notes, HttpStatus.OK);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getNotes(@PathVariable Long id) throws Exception {
        Notes notes = notesRepo.findById(id).orElse(null);
        return new ResponseEntity<>(notes, HttpStatus.OK);


    }
}
