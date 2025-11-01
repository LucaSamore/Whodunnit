import {defineConfig} from 'vitepress'

const reportPath = '/report'
const implementationPath = `${reportPath}/implementation`
const processPath = `${reportPath}/process`

// https://vitepress.dev/reference/site-config
export default defineConfig({
    base: '/PPS-24-whodunnit/',
    title: "Whodunnit",
    description: "A investigative game",
    themeConfig: {
        // https://vitepress.dev/reference/default-theme-config
        nav: [
            {text: 'Home', link: '/'}
        ],

        sidebar: [
            {
                text: 'Report',
                items: [
                    { text: 'Introduction', link: `${reportPath}/0-introduction` },
                    { text: 'Development Process', link: `${reportPath}/1-development-process` },
                    { text: 'Requirements', link: `${reportPath}/2-requirements` },
                    { text: 'High Level Design', link: `${reportPath}/3-high-level-design` },
                    { text: 'Detailed Design', link: `${reportPath}/4-detailed-design` },
                    {
                        text: 'Implementation',
                        items: [
                            {text: 'Lucia Castellucci', link: `${implementationPath}/5-castellucci`},
                            {text: 'Roberto Mitugno', link: `${implementationPath}/5-mitugno`},
                            {text: 'Luca Samorè', link: `${implementationPath}/5-samore`},
                        ]
                    },
                    { text: 'Testing', link: `${reportPath}/6-testing` },
                    { text: 'Retrospective', link: `${reportPath}/7-retrospective` },
                    {
                        text: 'Development Process Artifacts',
                        items: [
                            { text: 'Kickoff meeting', link: `${processPath}/8-kickoff-meeting` },
                            { text: 'Sprint 1', link: `${processPath}/8-sprint-1` },
                            { text: 'Sprint 2', link: `${processPath}/8-sprint-2` },
                            { text: 'Sprint 3', link: `${processPath}/8-sprint-3` },
                            { text: 'Sprint 4', link: `${processPath}/8-sprint-4` },
                            { text: 'Sprint 5', link: `${processPath}/8-sprint-5` },
                            { text: 'Sprint 6', link: `${processPath}/8-sprint-6` }
                        ]
                    }
                ]
            }
        ],

        socialLinks: [
            {icon: 'github', link: 'https://github.com/vuejs/vitepress'}
        ]
    }
})
