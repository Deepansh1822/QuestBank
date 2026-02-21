package in.sfp.main.controllers;

import in.sfp.main.models.Organisation;
import in.sfp.main.repository.OrganisationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/api/organisations")
public class OrganisationLogoController {

    @Autowired
    private OrganisationRepository organisationRepository;

    @PostMapping("/upload-logo")
    public ResponseEntity<?> uploadLogo(@RequestParam("organisation") String name,
            @RequestParam("logo") MultipartFile file) {
        try {
            Organisation org = new Organisation(name, file.getBytes(), file.getContentType());
            organisationRepository.save(org);
            return ResponseEntity.ok("Logo uploaded successfully for " + name);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/logo/{name}")
    public ResponseEntity<byte[]> getLogo(@PathVariable String name) {
        Optional<Organisation> org = organisationRepository.findById(name);
        if (org.isPresent() && org.get().getLogo() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(org.get().getContentType()))
                    .body(org.get().getLogo());
        }
        return ResponseEntity.notFound().build();
    }
}
