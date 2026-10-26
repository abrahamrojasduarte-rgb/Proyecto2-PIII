USE reservas;

INSERT INTO funcionario (id, nombre, telefono, clave, rol) VALUES
                                                               ('111', 'Juan Mora',     '88881111', '111', 'FUNCIONARIO'),
                                                               ('222', 'Maria Mata',    '88882222', '222', 'FUNCIONARIO'),
                                                               ('333', 'Pedro Arias',   '88883333', '333', 'FUNCIONARIO'),
                                                               ('444', 'Ana Lopez',     '88884444', '444', 'FUNCIONARIO');

INSERT INTO categoria (id, descripcion) VALUES
                                            ('CAT-000001', 'Sala para 10 personas'),
                                            ('CAT-000002', 'Laptop Windows'),
                                            ('CAT-000003', 'Sala de Juntas'),
                                            ('CAT-000004', 'Proyector'),
                                            ('CAT-000005', 'Microfono');

INSERT INTO recurso (id, descripcion, categoria_id) VALUES
                                                        ('SALA-101',   'Sala 1 primer piso',        'CAT-000001'),
                                                        ('SALA-102',   'Sala 2 primer piso',        'CAT-000001'),
                                                        ('LAP-238715', 'Laptop #238715',            'CAT-000002'),
                                                        ('LAP-238716', 'Laptop #238716',            'CAT-000002'),
                                                        ('JUN-201',    'Sala de juntas segundo piso','CAT-000003'),
                                                        ('PRO-001',    'Proyector Epson',           'CAT-000004'),
                                                        ('MIC-001',    'Microfono inalambrico',     'CAT-000005');