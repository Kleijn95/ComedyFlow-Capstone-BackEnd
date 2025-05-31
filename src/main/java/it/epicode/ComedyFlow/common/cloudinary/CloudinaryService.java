package it.epicode.ComedyFlow.common.cloudinary;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {
    @Autowired
    private Cloudinary cloudinary;
    public String uploadImage(MultipartFile file) {

        try {
            // folder è il nome della cartella dove l'immagine sarà salvata in cloudinary
            // public_id rappresenta il nome dell'immagine
            Map result = cloudinary.uploader()
                    .upload(file.getBytes(),  Cloudinary.asMap("folder", "FS1024", "public_id", file.getOriginalFilename()));

            // recupera dalla risposta di cloudinary l'url di visualizzazione dell'immagine
            // che può essere memorizzata in un database
            String url = result.get("secure_url").toString();

            return url;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}