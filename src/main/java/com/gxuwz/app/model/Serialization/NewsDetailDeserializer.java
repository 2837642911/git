package com.gxuwz.app.model.Serialization;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.gxuwz.app.model.network.NewsDetailResponse;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public  class NewsDetailDeserializer implements JsonDeserializer<NewsDetailResponse> {
    @Override
    public NewsDetailResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        NewsDetailResponse response = new Gson().fromJson(json, NewsDetailResponse.class);

        try {
            JsonObject resultObj = json.getAsJsonObject().getAsJsonObject("result");
            if (resultObj != null) {
                JsonObject detailObj = resultObj.getAsJsonObject("detail");
                if (detailObj != null) {
                    NewsDetailResponse.Detail detail = response.getResult().getDetail();
                    List<String> images = new ArrayList<>();

                    // 提取固定的thumbnail_pic_s
                    if (detailObj.has("thumbnail_pic_s") && !detailObj.get("thumbnail_pic_s").isJsonNull()) {
                        images.add(detailObj.get("thumbnail_pic_s").getAsString());
                    }

                    // 动态提取所有以"thumbnail_pic_s"开头的字段
                    for (Map.Entry<String, JsonElement> entry : detailObj.entrySet()) {
                        String key = entry.getKey();
                        if (key.startsWith("thumbnail_pic_s") && !key.equals("thumbnail_pic_s")
                                && entry.getValue().isJsonPrimitive()) {
                            String url = entry.getValue().getAsString();
                            if (url != null && !url.isEmpty()) {
                                images.add(url);
                            }
                        }
                    }

                    detail.setAllImages(images);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }
}