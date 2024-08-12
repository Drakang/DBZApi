package API;

import java.util.List;

public class test {
    private DragonBallApi hero;

    public void setInstance(DragonBallApi heroInstance) {
        this.hero = heroInstance;
    }

    public static void main(String[] args) {
        // Añadir correo y contraseña
        String email = "bejl@keepcoding.es";
        String password = "123456";

        DragonBallApi hero = new DragonBallApi();
        hero.setDebugMode(true); // Habilitar modo depuración para los logs

        int loginResult = hero.login(email, password);
        if (loginResult == DragonBallApi.ERROR) {
            System.out.println("Error login");
            return;
        }
        System.out.println("Token: " + hero.getToken());
        System.out.println();

        String token = hero.getToken();
        String name = "";
        hero.listHeroes(token, name);

        List<Heroes> heroesList = DragonBallApi.getHeroList(); 
        for (int i = 0; i < heroesList.size(); i++) {
            System.out.println("ID: " + hero.getHeroId(i));
            System.out.println("Nombre: " + hero.getHeroName(i));
            System.out.println("Descripción: " + hero.getHeroDescription(i));
            System.out.println("Foto: " + hero.getHeroPhoto(i));
            System.out.println();

            // Imprimir las evoluciones
            System.out.println("Evoluciones:");
            List<Heroes> evolutions = heroesList.get(i).getEvolutions();
            for (int j = 0; j < evolutions.size(); j++) {
                System.out.println("\tID Evolución: " + hero.getEvolutionId(i, j));
                System.out.println("\tNombre Evolución: " + hero.getEvolutionName(i, j));
                System.out.println("\tDescripción Evolución: " + hero.getEvolutionDescription(i, j));
                System.out.println("\tFoto Evolución: " + hero.getEvolutionPhoto(i, j));
                System.out.println();
            }
            System.out.println("----------------------------------------------------");
        }

        // Contar el total de evoluciones
        int totalEvolutions = hero.getTotalEvolutionsCount();
        System.out.println("Total de Evoluciones: " + totalEvolutions);

        int countLogs = hero.getLogsCount();
        for (int i = 0; i < countLogs; i++) {
            System.out.println("Logs: " + hero.getLog(i));
        }
    }
}
