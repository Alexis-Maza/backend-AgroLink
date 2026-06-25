//package AgroLink.AgroLink.domain.service;
//
//import com.cloudinary.Cloudinary;
//import com.cloudinary.utils.ObjectUtils;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class CloudinaryService {
//
//    private final Cloudinary cloudinary;
//
//    public String uploadImage(MultipartFile file, String folder) throws IOException {
//        Map uploadResult = cloudinary.uploader().upload(
//                file.getBytes(),
//                ObjectUtils.asMap(
//                        "folder", folder,
//                        "resource_type", "image"
//                )
//        );
//        return (String) uploadResult.get("secure_url");
//    }
//
//    public void deleteImage(String imageUrl) throws IOException {
//        if (imageUrl == null || imageUrl.isEmpty()) return;
//
//        // Extraer el public_id de la URL
//        String publicId = extractPublicId(imageUrl);
//        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
//    }
//
//    private String extractPublicId(String imageUrl) {
//        // URL formato: https://res.cloudinary.com/cloud/image/upload/v123/folder/filename.jpg
//        String[] parts = imageUrl.split("/upload/");
//        String afterUpload = parts[1];
//        // Quitar versión si existe (v123/)
//        if (afterUpload.startsWith("v")) {
//            afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
//        }
//        // Quitar extensión
//        return afterUpload.substring(0, afterUpload.lastIndexOf("."));
//    }
//}