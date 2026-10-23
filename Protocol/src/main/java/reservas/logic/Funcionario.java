package reservas.logic;

public class Funcionario extends Usuario {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private String telefono;

    public Funcionario() {
        super();
    }

    public Funcionario(String id, String nombre, String telefono) {
        super(id, id, Rol.FUNCIONARIO);   // clave inicial = id
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}