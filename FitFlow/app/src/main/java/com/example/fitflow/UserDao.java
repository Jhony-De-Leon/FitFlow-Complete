package com.example.fitflow;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update; // Importar la anotación Update

@Dao
public interface UserDao {

    // Inserta un nuevo usuario. Si el email ya existe (debido a la restricción UNIQUE en la entidad),
    // la estrategia OnConflictStrategy.ABORT hará que la operación falle (lo cual es bueno para el registro).
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void registerUser(User user);

    // Busca un usuario por su email. Útil para el login y para verificar si un email ya está registrado.
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    // Busca un usuario por su ID. Útil después del login para obtener los datos del usuario logueado.
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserById(int userId);

    // Método para actualizar un usuario existente.
    @Update
    void updateUser(User user);
    
    // @Delete
    // void deleteUser(User user);
}
