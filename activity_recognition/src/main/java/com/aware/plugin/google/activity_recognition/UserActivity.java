package com.aware.plugin.google.activity_recognition;

import org.json.JSONArray;

/*
    Declaração da classe UserActivity.
*/
public class UserActivity {

    /*
        Declaração do atributo activity_name onde será guardado o nome da atividade.
    */
    private static String activity_name;

    /*
        Declaração do atributo activity_type onde será guardado o tipo de atividade (int) que corresponde à
        atividade retornada pela Activity Recogniton API.
    */
    private static int activity_type;

    /*
        Declaração do atributo confidence onde será guardado o nível de confiança para cada actividade detetada.
    */
    private static int confidence;

    /*
        Declaração do atributo activities onde serão guardadas todas as atividades detetadas pelo dispositivo num determinado momento
        e também os respetivos níveis de confiança para cada uma dessas atividades.
    */
    private static JSONArray activities;

    /*
        Construtor Default
    */
    public UserActivity(){
        super();
    }

    /*
        Declaração do método getter para retornar o nome da actividade.
    */
    public static String getActivityName() {
        return activity_name;
    }

    /*
        Declaração do método getter para retornar o tipo de atividade.
    */
    public static int getActivityType(){
        return activity_type;
    }

    /*
        Declaração do método getter para retornar o nível de confiança da atividade detetada.
    */
    public static int getConfidence() {
        return confidence;
    }

    /*
        Declaração do método getter para retornar todas as atividades detetadas pelo dispositivo num determinado momento
        e os respetivos níveis de confiança.
    */
    public static JSONArray getActivities(){
        return activities;
    }

    /*
        Declaração do método setter para modificar o nome da atividade.
    */
    static void setActivityName(String activity_name) {
        UserActivity.activity_name = activity_name;
    }

    /*
        Declaração do método setter para modificar o tipo de atividade.
    */
    static void setActivityType(int activity_type){
        UserActivity.activity_type = activity_type;
    }

    /*
        Declaração do método setter para modificar o nível de confiança de uma determinada atividade.
    */
    static void setConfidence(int confidence){
        UserActivity.confidence = confidence;
    }

    /*
        Declaração do método setter para modificar as atividades e os respetivos níveis de confiança num determinado momento.
    */
    static void setActivities(JSONArray activities){
        UserActivity.activities = activities;
    }
}