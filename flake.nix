{
  description = "honeysql-page";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-25.11";
  };
  outputs =
    {
      self,
      nixpkgs,
      ...
    }:
    let
      supportedSystems = [
        "aarch64-darwin"
        "aarch64-linux"
        "x86_64-darwin"
        "x86_64-linux"
      ];
      forAllSystems =
        function:
        nixpkgs.lib.genAttrs supportedSystems (
          system:
          function system (
            import nixpkgs {
              inherit system;
            }
          )
        );
    in
    {
      devShells = forAllSystems (
        system: pkgs: {
          default = pkgs.mkShell {
            buildInputs = with pkgs; [
              awscli2
              babashka
              cacert
              clojure
              curl
              glibcLocales # rlwrap (used by clj) uses this
              gnused
              jq
              nodejs
              nodePackages.npm
              rlwrap
            ];
          };
        }
      );
    };
}
