Feature: Como usuario de google quiero buscar la palabra simetrik y que no me traiga cero resultados

  @BuscarPalabra
  Scenario: Verificar el funcionamiento del motor de busqueda
    Given ir al navegador de Google Chrome
    When digitar la palabra "simetrik" en el buscador
    And dar enter para que se inicie la busqueda
    Then validar que el total de resultados de consulta sea diferente a cero

  @BuscarSelenium
  Scenario: Verificar la busqueda de la palabra selenium
    Given ir al navegador de Google Chrome
    When digitar la palabra "selenium" en el buscador
    And dar enter para que se inicie la busqueda
    Then validar que el total de resultados de consulta sea diferente a cero

  @BuscarTituloCucumber
  Scenario: Verificar que el título de resultados contenga la palabra buscada
    Given ir al navegador de Google Chrome
    When digitar la palabra "cucumber" en el buscador
    And dar enter para que se inicie la busqueda
    Then validar que el título de la página contenga la palabra "cucumber"

  @CampoBusquedaVisible
  Scenario: Verificar que el campo de búsqueda está visible al ingresar a Google
    Given ir al navegador de Google Chrome
    Then el campo de búsqueda debe estar visible

  @TituloPaginaInicial
  Scenario: Verificar el título de la página de inicio de Google
    Given ir al navegador de Google Chrome
    Then el título de la página de inicio debe ser "Google"

  @CampoBusquedaEditable
  Scenario: Verificar que se puede escribir en el campo de búsqueda sin enviar la búsqueda
    Given ir al navegador de Google Chrome
    When escribir "simetrik" en el campo de búsqueda sin enviar
    Then el campo de búsqueda debe contener el texto "simetrik"

  @BuscarFraseCompuesta
  Scenario: Verificar una búsqueda de varias palabras
    Given ir al navegador de Google Chrome
    When digitar la palabra "simetrik selenium" en el buscador
    And dar enter para que se inicie la busqueda
    Then validar que el título de la página contenga la palabra "simetrik"