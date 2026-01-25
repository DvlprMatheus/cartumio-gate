FROM mcr.microsoft.com/devcontainers/java:1-21-bullseye

ARG INSTALL_MAVEN="false"
ARG MAVEN_VERSION=""

ARG INSTALL_GRADLE="true"
ARG GRADLE_VERSION=""

# Remove or fix expired Yarn repository GPG key
RUN rm -f /etc/apt/sources.list.d/yarn.list || true \
    && rm -f /etc/apt/trusted.gpg.d/yarn.gpg || true

# Install zsh and configure shell history
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends zsh curl git \
    && rm -rf /var/lib/apt/lists/*

# Install oh-my-zsh for vscode user
RUN su vscode -c "sh -c \"$(curl -fsSL https://raw.githubusercontent.com/ohmyzsh/ohmyzsh/master/tools/install.sh)\" \"\" --unattended" || true

# Configure zsh as default shell for vscode user
RUN chsh -s $(which zsh) vscode

# Configure shell history persistence
RUN mkdir -p /home/vscode/.zsh_history.d /dc/shellhistory \
    && touch /home/vscode/.zsh_history \
    && chown -R vscode:vscode /home/vscode/.zsh_history* /dc/shellhistory \
    && echo 'export HISTFILE="/dc/shellhistory/zsh_history"' >> /home/vscode/.zshrc \
    && echo 'export HISTSIZE=10000' >> /home/vscode/.zshrc \
    && echo 'export SAVEHIST=10000' >> /home/vscode/.zshrc \
    && echo 'setopt SHARE_HISTORY' >> /home/vscode/.zshrc \
    && echo 'setopt APPEND_HISTORY' >> /home/vscode/.zshrc

RUN if [ "${INSTALL_MAVEN}" = "true" ]; then su vscode -c "umask 0002 && . /usr/local/sdkman/bin/sdkman-init.sh && sdk install maven \"${MAVEN_VERSION}\""; fi \
    && if [ "${INSTALL_GRADLE}" = "true" ]; then su vscode -c "umask 0002 && . /usr/local/sdkman/bin/sdkman-init.sh && sdk install gradle \"${GRADLE_VERSION}\""; fi

# [Optional] Uncomment this section to install additional OS packages.
# RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
#     && apt-get -y install --no-install-recommends <your-package-list-here>

# [Optional] Uncomment this line to install global node packages.
# RUN su vscode -c "source /usr/local/share/nvm/nvm.sh && npm install -g <your-package-here>" 2>&1