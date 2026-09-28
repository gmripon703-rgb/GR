package com.example.modelmanager

object DefaultModelCatalog {

    val curatedModels = listOf(
        ModelInfo(
            id = "qwen2.5-coder-0.5b-q4",
            name = "Qwen 2.5 Coder 0.5B (GGUF)",
            version = "1.0.0",
            format = "GGUF",
            quantization = "Q4_K_M",
            sizeBytes = 398000000L, // ~380 MB
            minimumRamGb = 2.0f,
            recommendedRamGb = 3.0f,
            minimumStorageBytes = 600000000L,
            architecture = "qwen2",
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-Coder-0.5B-Instruct-GGUF/resolve/main/qwen2.5-coder-0.5b-instruct-q4_k_m.gguf",
            sha256 = "b687fba575d5a69894cf9659b867c4ec113fe668dc44f07a721c5b8b9b47bb99",
            license = "Apache-2.0",
            sourceUrl = "https://huggingface.co/Qwen/Qwen2.5-Coder-0.5B-Instruct-GGUF",
            author = "Qwen Alibaba",
            category = "CODING",
            description = "Ultra-lightweight code generation and completion model. Runs smoothly on budget Android phones with 3GB+ RAM."
        ),
        ModelInfo(
            id = "qwen2.5-coder-1.5b-q4",
            name = "Qwen 2.5 Coder 1.5B (GGUF)",
            version = "1.0.0",
            format = "GGUF",
            quantization = "Q4_K_M",
            sizeBytes = 986000000L, // ~940 MB
            minimumRamGb = 3.5f,
            recommendedRamGb = 5.0f,
            minimumStorageBytes = 1400000000L,
            architecture = "qwen2",
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-Coder-1.5B-Instruct-GGUF/resolve/main/qwen2.5-coder-1.5b-instruct-q4_k_m.gguf",
            sha256 = "679f22580a58a6ee5f272a8d5f30999554a938c5a0ec7b8cfd2a93da12484433",
            license = "Apache-2.0",
            sourceUrl = "https://huggingface.co/Qwen/Qwen2.5-Coder-1.5B-Instruct-GGUF",
            author = "Qwen Alibaba",
            category = "CODING",
            description = "High-accuracy coding copilot for Kotlin, Java, Python, C++, and Bash. Recommended for phones with 4GB+ RAM."
        ),
        ModelInfo(
            id = "smollm2-360m-instruct-q4",
            name = "SmolLM2 360M Instruct (GGUF)",
            version = "1.0.0",
            format = "GGUF",
            quantization = "Q4_K_M",
            sizeBytes = 228000000L, // ~218 MB
            minimumRamGb = 1.5f,
            recommendedRamGb = 2.0f,
            minimumStorageBytes = 400000000L,
            architecture = "llama",
            downloadUrl = "https://huggingface.co/HuggingFaceTB/SmolLM2-360M-Instruct-GGUF/resolve/main/smollm2-360m-instruct-q4_k_m.gguf",
            sha256 = "c081e843c0d8d011f122ec01a74d2847c23f2f84cb7cb9f67a2168962657e335",
            license = "Apache-2.0",
            sourceUrl = "https://huggingface.co/HuggingFaceTB/SmolLM2-360M-Instruct-GGUF",
            author = "HuggingFaceTB",
            category = "GENERAL",
            description = "Pocket-sized offline assistant for reasoning, terminal command explanations, and markdown summaries."
        ),
        ModelInfo(
            id = "llama-3.2-1b-instruct-q4",
            name = "Llama 3.2 1B Instruct (GGUF)",
            version = "1.0.0",
            format = "GGUF",
            quantization = "Q4_K_M",
            sizeBytes = 804000000L, // ~760 MB
            minimumRamGb = 3.0f,
            recommendedRamGb = 4.0f,
            minimumStorageBytes = 1200000000L,
            architecture = "llama",
            downloadUrl = "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q4_K_M.gguf",
            sha256 = "8e9bcf20d6b5e58ea9c811568e27c191a0b5f12e840a4dfb80f82163354b3a12",
            license = "Llama 3.2 Community",
            sourceUrl = "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF",
            author = "Meta / Bartowski",
            category = "GENERAL",
            description = "Defensive cybersecurity reasoning, threat model explanation, and general developer queries."
        ),
        ModelInfo(
            id = "deepseek-coder-1.3b-q4",
            name = "DeepSeek Coder 1.3B (GGUF)",
            version = "1.0.0",
            format = "GGUF",
            quantization = "Q4_K_M",
            sizeBytes = 874000000L, // ~830 MB
            minimumRamGb = 3.0f,
            recommendedRamGb = 4.0f,
            minimumStorageBytes = 1300000000L,
            architecture = "deepseek",
            downloadUrl = "https://huggingface.co/TheBloke/deepseek-coder-1.3b-instruct-GGUF/resolve/main/deepseek-coder-1.3b-instruct.Q4_K_M.gguf",
            sha256 = "e2c38f16b251ce7efad2a0dca0bb7a33eeef117d7b3858fa7ee90a424e4f8809",
            license = "DeepSeek License (Permissive)",
            sourceUrl = "https://huggingface.co/TheBloke/deepseek-coder-1.3b-instruct-GGUF",
            author = "DeepSeek AI",
            category = "CODING",
            description = "Specialized coding LLM trained from scratch on 2T tokens of code and natural language."
        )
    )
}
