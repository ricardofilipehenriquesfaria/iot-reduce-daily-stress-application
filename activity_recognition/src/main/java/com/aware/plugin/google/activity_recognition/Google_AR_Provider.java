package com.aware.plugin.google.activity_recognition;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.Log;

import com.aware.Aware;
import com.aware.utils.DatabaseHelper;

import java.util.HashMap;

/*
    O ContentProvider funciona como um gateway para os dados da aplicação, fornecendo uma interface padrão para que as aplicações possam expor e consultar informações.
    Embora os ContentProviders possam utilizar qualquer forma de armazenamento de dados disponível na plataforma Android,
    incluindo arquivos, mapas hash ou sistemas de preferências, neste caso será utilizada uma base de dados SQLite.
    O ContentProvider implementa um conjunto padrão de métodos para permitir o acesso à base de dados.
    O seu comportamento baseia-se nos métodos utilizados numa base de dados, onde podemos fazer insert(), update(), delete() e query().
*/
public class Google_AR_Provider extends ContentProvider {

    /*
        A constante DATABASE_VERSION possuirá o número da versão da base de dados do ContentProvider.
        O número da versão deve ser incrementado por 1 sempre que a estrutura da base de dados seja modificada,
        de modo a que o Android tenha conhecimento da mudança.
        A classe DatabaseHelper que estende a classe SQLiteOpenHelper irá ajudar a migrar a estrutura da base de dados para futuras versões da aplicação.
        Sempre que a base de dados seja acedida, mas a versão existente em disco não corresponda à versão atual
        (ou seja, à versão passada pelo construtor), será chamado o método onUpgrade() da classe DatabaseHelper.
    */
    private static final int DATABASE_VERSION = 1;

    /*
        Os índices URI servem para que a gestão de bases de dados do Android encontre entradas ou coleções de entradas na base de dados.
        Cada um tem um índice específico, neste caso o collection index (GOOGLE_AR) tem como valor 1 e o item index (GOOGLE_AR_ID) tem como valor 2.
        O GOOGLE_AR será utilizado quando for requisitada uma coleção ou lista de entradas.
        O GOOGLE_AR_ID será utilizado quando apenas uma entrada é requisitada através de um _ID.
    */
    private static final int GOOGLE_AR = 1;
    private static final int GOOGLE_AR_ID = 2;

    /*
        A constante DATABASE_NAME define o nome da base de dados, tal como esta será guardada no smartphone.
    */
    public static final String DATABASE_NAME = "plugin_google_activity_recognition.db";

    /*
        A variável AUTHORITY é uma sequência de caracteres que é utilizada para identificar o ContentProvider de forma exclusiva,
        sendo necessária para realizar transações e queries na base de dados.
        Por isso esta tem de ser diferente de qualquer AUTHORITY de outro ContentProvider que possa existir no dispositivo móvel,
        e consequentemente no repositório da Framework Aware.
    */
    public static String AUTHORITY = "app.miti.com.iot_reduce_daily_stress_application.provider.gar";

    /*
        Esta é uma interface onde são declaradas as "constantes" base (Framework AWARE) para a criação das colunas das tabelas da base de dados.
    */
    public interface AWAREColumns extends BaseColumns {
        String _ID = "_id";
        String TIMESTAMP = "timestamp";
        String DEVICE_ID = "device_id";
    }

    /*
        Esta é uma Contract class que atua como um container para as constantes, definindo nomes para as URIs e para as colunas da base de dados.
        Ao implementar a interface AWAREColumns, são herdados os campos base da Framework Aware, que serão utilizados na tabela da base de dados.
        Como a interface AWAREColumns estende (extends) a interface BaseColumns,
        é herdado um campo _ID (chave primária) que será utilizado como id de incremento automático na tabela da base de dados.
    */
    static final class Google_Activity_Recognition_Data implements AWAREColumns {

        /*
            Construtor Default
        */
        private Google_Activity_Recognition_Data() {}

        /*
            O CONTENT_URI é um URI que identifica os dados num ContentProvider.
            O CONTENT_URI contém o nome simbólico do ContentProvider (AUTHORITY) e um nome que aponta para uma tabela (path).
            Quando um pedido é feito para aceder a uma tabela de um ContentProvider, o CONTENT_URI da tabela é um dos argumentos.
        */
        static final Uri CONTENT_URI = Uri.parse("content://" + Google_AR_Provider.AUTHORITY + "/plugin_google_activity_recognition");

        /*
            O tipo MIME dos resultados do CONTENT_URI quando um valor específico de _ID não é fornecido, sendo que podem ser retornados vários registos.
        */
        static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.aware.plugin.google.activity_recognition";

        /*
            O tipo MIME dos resultados quando um _ID é anexado ao CONTENT_URI, retornando um único registo.
        */
        static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.aware.plugin.google.activity_recognition";

        static final String ACTIVITY_NAME = "activity_name";
        static final String ACTIVITY_TYPE = "activity_type";
        static final String CONFIDENCE = "confidence";
        static final String ACTIVITIES = "activities";
    }

    /*
        Outra constante necessária é a DATABASE_TABLES, um array de Strings que possui todas as tabelas existentes no ContentProvider,
        sendo que neste caso apenas irá possuir uma tabela.
        De salientar que o nome da tabela tem de corresponder ao último segmento da constante CONTENT_URI.
    */
    public static final String[] DATABASE_TABLES = {
            "plugin_google_activity_recognition"
    };

    /*
        Para preparar a base de dados SQLite, criamos os campos da tabela da base de dados,
        com os seus tipos de dados e respetivos valores por defeito.
    */
    public static final String[] TABLES_FIELDS = {
            Google_Activity_Recognition_Data._ID + " integer primary key autoincrement," + 
            Google_Activity_Recognition_Data.TIMESTAMP + " real default 0," + 
            Google_Activity_Recognition_Data.DEVICE_ID + " text default ''," +
            Google_Activity_Recognition_Data.ACTIVITY_NAME + " text default ''," +
            Google_Activity_Recognition_Data.ACTIVITY_TYPE + " integer default 0," +
            Google_Activity_Recognition_Data.CONFIDENCE + " integer default 0," +
            Google_Activity_Recognition_Data.ACTIVITIES + " text default ''," +
            "UNIQUE (" + Google_Activity_Recognition_Data.TIMESTAMP + "," + Google_Activity_Recognition_Data.DEVICE_ID + ")"
    };

    /*
        Quando alguma operação é solicitada ao ContentProvider, será necessário determinar a partir da URI o que deve ser feito.
        Para isso, será utilizada a classe UriMatcher, que faz a comparação entre a URI recebida com as URIs já definidas.
    */
    private static UriMatcher sUriMatcher = null;

    /*
        O HashMap <String, String> irá conter as colunas da tabela da base de dados.
    */
    private static HashMap<String, String> gARMap = null;

    /*
        O DatabaseHelper é utilizado como uma classe utilitária para gerir a base de dados.
    */
    private DatabaseHelper databaseHelper = null;

    /*
        O SQLiteDatabase exporta os métodos necessários para gerir uma base de dados SQLite.
    */
    private static SQLiteDatabase database = null;

    /*
        O método initializeDatabase() permite inicializar o ContentProvider.
    */
    private boolean initializeDatabase() {

        if(databaseHelper == null) databaseHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS);

        if(database == null || !database.isOpen()) database = databaseHelper.getWritableDatabase();

        return (database != null && databaseHelper != null);
    }

    /*
        O método delete() é o responsável por eliminar um registo (ou registos) com base na cláusula WHERE que lhe foi passada.
        Primeiro, o tipo de URI passado é determinado através de uma chamada ao método match() do UriMatcher.
        Se o tipo de URI for do tipo collection, a cláusula WHERE é passada de modo a que possam ser excluídos tantos registos quanto possível.
        Se a cláusula WHERE for null, todos os registos serão excluídos.
        Se o tipo de URI for de registo único (single-record), O ID é extraído do URI, sendo especificada uma cláusula WHERE adicional.
        Se a eliminação tiver sido bem sucedida, um URI é criado para notificar o sistema de uma alteração nos dados,
        através de uma chamada ao método notifyChange() do ContentResolver.
        No final, o código retorna o número de registos excluídos.
    */
    @SuppressLint("LongLogTag")
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        int count;

        if(!initializeDatabase()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return 0;
        }

        switch (sUriMatcher.match(uri)) {
            case GOOGLE_AR:
                count = database.delete(DATABASE_TABLES[0], selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (getContext() != null) getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    /*
        O método getType() retorna o tipo de dados que serão devolvidos pelo pedido de um URI específico.
    */
    @Override
    public String getType(@NonNull Uri uri) {

        switch (sUriMatcher.match(uri)) {
            case GOOGLE_AR:
                return Google_Activity_Recognition_Data.CONTENT_TYPE;
            case GOOGLE_AR_ID:
                return Google_Activity_Recognition_Data.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    /*
        O método insert() é responsável por inserir um registo na base de dados e retornar um URI que aponte para o registo recém-criado.
        Primeiro, o tipo de URI passado é determinado através de uma chamada ao método match() do UriMatcher.
        O URI é então validado para se certificar de que o tipo de URI é apropriado, de modo a que a inserção faça sentido.
        Caso contrário, o código lança uma exceção.
        Em seguida, o código utiliza um objeto SQLiteDatabase para inserir um novo registo, retornando o ID do registo recém-inserido.
        Se a inserção tiver sido bem sucedida, um URI é criado para notificar o sistema de uma alteração nos dados,
        através de uma chamada ao método notifyChange() do ContentResolver.
        Caso contrário, uma exceção é lançada.
    */
    @SuppressLint("LongLogTag")
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues initialValues) {

        ContentValues values = (initialValues != null) ? new ContentValues(initialValues) : new ContentValues();

        if(!initializeDatabase()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return null;
        }

        switch (sUriMatcher.match(uri)) {
            case GOOGLE_AR:
                long google_AR_id = database.insert(DATABASE_TABLES[0], Google_Activity_Recognition_Data.ACTIVITY_NAME, values);

                if (google_AR_id > 0) {
                    Uri new_uri = ContentUris.withAppendedId(Google_Activity_Recognition_Data.CONTENT_URI, google_AR_id);
                    if (getContext() != null) getContext().getContentResolver().notifyChange(new_uri, null);
                    return new_uri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    /*
        O método OnCreate() é chamado quando o ContentProvider é criado pela primeira vez,
        sendo também utilizado para executar todas as tarefas de inicialização necessárias.
    */
    @Override
    public boolean onCreate() {

        if (getContext() != null) AUTHORITY = getContext().getPackageName() + ".provider.gar";

    	sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Google_AR_Provider.AUTHORITY, DATABASE_TABLES[0], GOOGLE_AR);
        sUriMatcher.addURI(Google_AR_Provider.AUTHORITY, DATABASE_TABLES[0] + "/#", GOOGLE_AR_ID);

        gARMap = new HashMap<>();
        gARMap.put(Google_Activity_Recognition_Data._ID, Google_Activity_Recognition_Data._ID);
        gARMap.put(Google_Activity_Recognition_Data.TIMESTAMP, Google_Activity_Recognition_Data.TIMESTAMP);
        gARMap.put(Google_Activity_Recognition_Data.DEVICE_ID, Google_Activity_Recognition_Data.DEVICE_ID);
        gARMap.put(Google_Activity_Recognition_Data.ACTIVITY_NAME, Google_Activity_Recognition_Data.ACTIVITY_NAME);
        gARMap.put(Google_Activity_Recognition_Data.ACTIVITY_TYPE, Google_Activity_Recognition_Data.ACTIVITY_TYPE);
        gARMap.put(Google_Activity_Recognition_Data.CONFIDENCE, Google_Activity_Recognition_Data.CONFIDENCE);
        gARMap.put(Google_Activity_Recognition_Data.ACTIVITIES, Google_Activity_Recognition_Data.ACTIVITIES);
    	
        return true;
    }

    /*
        O método query() permite que o utilizador realize consultas às tabelas da base de dados.
        Primeiro, o código obtém uma instância de um objeto SQLiteQueryBuilder que irá formular e executar a query.
        Depois, o tipo de URI passado é determinado com uma chamada ao método match() do UriMatcher.
        Então, o método setTables() configura qual será a tabela da base de dados a ser consultada.
        Finalmente, é criado um Cursor que irá verificar se os dados de origem mudaram, através do método setNotificationUri().
        Este procedimento é necessário pois o método query() da aplicação pode ser chamado por várias threads,
        e uma vez que este realiza chamadas para o método update(),
        é possível que os dados possam ser alterados após o retorno do cursor.
        Fazer isto também faz com que se mantenham os dados sincronizados.
    */
    @SuppressLint("LongLogTag")
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        
    	if(!initializeDatabase()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return null;
        }

        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            case GOOGLE_AR:
                sqLiteQueryBuilder.setTables(DATABASE_TABLES[0]);
                sqLiteQueryBuilder.setProjectionMap(gARMap);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        try {
            Cursor cursor = sqLiteQueryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
            if (getContext() != null) cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        } catch (IllegalStateException e) {
            if (Aware.DEBUG) Log.e(Aware.TAG, e.getMessage());
            return null;
        }
    }

    /*
        O método update() permite que sejam atualizados os registos já existentes na base de dados.
        Primeiro, o tipo de URI passado é determinado com uma chamada ao método match() do UriMatcher.
        Depois de chamar o método update() apropriado, o sistema é notificado da alteração ao URI através de uma chamada ao método notifyChange().
        Isto irá informar qualquer Observer do URI que os dados possivelmente mudaram.
        Finalmente, é retornado o número de linhas afetadas.
    */
    @SuppressLint("LongLogTag")
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int count;

    	if(!initializeDatabase()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return 0;
        }

        switch (sUriMatcher.match(uri)) {
            case GOOGLE_AR:
                count = database.update(DATABASE_TABLES[0], values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (getContext() != null) getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}