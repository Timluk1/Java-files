package org.example.util;

import com.cloudinary.Cloudinary;

public class CloudinaryUtil {
    private static Cloudinary cloudinary;

    public static Cloudinary getInstance() {
        if (CloudinaryUtil.cloudinary == null) {
            cloudinary = new Cloudinary("");
        }
        return cloudinary;
    }
}
