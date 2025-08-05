{
  description = "honeysql-page";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-25.05";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils, ... }:
    flake-utils.lib.eachDefaultSystem (system:
      with import nixpkgs { inherit system; };
      with pkgs; {
        devShells.default = mkShell {
          buildInputs = [
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
      });
}
