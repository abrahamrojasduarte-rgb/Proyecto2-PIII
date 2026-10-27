package reservas.logic;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ReservaExtractorService {

    @SystemMessage("""
            Eres un asistente especializado en extraer informacion de reservas de espacios y recursos.
            Reglas estrictas:
            1. La fecha siempre debe estar en formato ISO yyyy-MM-dd. Si el usuario dice "proximo 12 de agosto"
               y el anio no esta especificado, usa el anio actual o el siguiente si la fecha ya paso.
            2. Las horas deben estar en formato HH:mm de 24 horas (ej. 8am -> "08:00", 10pm -> "22:00").
            3. Para 'categoriasRecurso' SOLO puedes usar nombres que aparezcan EXACTAMENTE en la lista
               de categorias disponibles proporcionada. No inventes categorias nuevas.
            4. Si algun campo no esta en la frase, devuelve null para ese campo.
            """)
    @UserMessage("""
            Fecha de referencia (hoy): {{hoy}}
            Categorias de recurso disponibles en el sistema:
            {{categorias}}
            Frase del usuario:
            "{{frase}}"
            Extrae los datos de la reserva seleccionando las categorias que mejor correspondan
            a lo que el usuario describe.
            """)
    ReservaExtraccion extraer(@V("frase") String frase,
                              @V("categorias") String categoriasList,
                              @V("hoy") String fechaHoy);
}