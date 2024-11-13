package fr.mesrouxvais.wifmap.applicationapi;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/upload")
public class UploadHandler {
	
	
	
	@PostMapping("/{session}/{friend}")
    void create(@PathVariable String session, @PathVariable String friend) {
		
        
    }
}
